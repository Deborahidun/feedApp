package com.bptn.feedApp.filter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.OPTIONS;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.bptn.feedApp.provider.ResourceProvider;
import com.bptn.feedApp.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	@Autowired
	@Qualifier("handlerExceptionResolver")
	HandlerExceptionResolver resolver;

	/**
	 * This method returns an Authentication object for the provided username.
	 */
	private Authentication getAuthentication(String username, HttpServletRequest req) {
		UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(username,
				null, null);
		userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
		return userPasswordAuthToken;
	}

	/**
	 * This method checks if the JWT prefix in the header is valid.
	 */
	private boolean isJwtPrefixValid(String header) {
		logger.debug("Authorization Header: {}", Optional.ofNullable(header).orElse("Not Present"));

		return Optional.ofNullable(header).filter(h -> h.startsWith(this.provider.getJwtPrefix())).isPresent();
	}

	/**
	 * This method is called once per request to check the JWT token and
	 * authenticate the user.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
			throws ServletException, IOException {

		logger.debug("Running Jwt Filter, URL: {}, Method: {}", req.getRequestURL(), req.getMethod());

		try {
			// Skip validation for OPTIONS requests.
			if (!req.getMethod().equalsIgnoreCase(OPTIONS.name())) {

				// Get the Authorization header from the request.
				String header = req.getHeader(AUTHORIZATION);

				// Validate the JWT prefix and check the header content.
				if (this.isJwtPrefixValid(header)) {
					// Extract the username from the JWT token.
					String username = this.jwtService.getSubject(header.substring(7));

					// Set the username in the Spring Security context.
					SecurityContextHolder.getContext().setAuthentication(this.getAuthentication(username, req));

					logger.debug("User Authorized: {}", username);
				}
			}

			// Proceed with the filter chain.
			filterChain.doFilter(req, res);
		} catch (JWTVerificationException ex) {
			logger.debug("Token cannot be verified, Reason: {}", ex.getMessage());

			// Handle the exception using the resolver.
			this.resolver.resolveException(req, res, null, ex);
		}
	}
}
