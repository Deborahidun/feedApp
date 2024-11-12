package com.bptn.feedApp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bptn.feedApp.exception.domain.EmailExistException;
import com.bptn.feedApp.exception.domain.EmailNotVerifiedException;
import com.bptn.feedApp.exception.domain.UserNotFoundException;
import com.bptn.feedApp.exception.domain.UsernameExistException;
import com.bptn.feedApp.jpa.Profile;
import com.bptn.feedApp.jpa.User;
import com.bptn.feedApp.provider.ResourceProvider;
import com.bptn.feedApp.repository.UserRepository;
import com.bptn.feedApp.security.JwtService;

@Service
public class UserService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	// Method to list all users
	public List<User> listUsers() {
		return this.userRepository.findAll();
	}

	// Method to find a user by username
	public Optional<User> findByUsername(String username) {
		return this.userRepository.findByUsername(username);
	}

	// Method to create a new user
	public void createUser(User user) {
		this.userRepository.save(user);
	}

	// New method to validate username and email for duplication
	private void validateUsernameAndEmail(String username, String emailId) {
		this.userRepository.findByUsername(username).ifPresent(u -> {
			throw new UsernameExistException(String.format("Username already exists, %s", u.getUsername()));
		});

		this.userRepository.findByEmailId(emailId).ifPresent(u -> {
			throw new EmailExistException(String.format("Email already exists, %s", u.getEmailId()));
		});
	}

	// Updated signup method with validation and password encryption
	public User signup(User user) {
		user.setUsername(user.getUsername().toLowerCase());
		user.setEmailId(user.getEmailId().toLowerCase());

		this.validateUsernameAndEmail(user.getUsername(), user.getEmailId());

		user.setEmailVerified(false);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		user.setCreatedOn(Timestamp.from(Instant.now()));

		this.userRepository.save(user);
		this.emailService.sendVerificationEmail(user);

		return user;
	}

	// Static method to check if email is verified
	private static User isEmailVerified(User user) {
		if (user.getEmailVerified().equals(false)) {
			throw new EmailNotVerifiedException(String.format("Email requires verification, %s", user.getEmailId()));
		}

		return user;
	}

	// Method to verify email of the logged-in user
	public void verifyEmail() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		user.setEmailVerified(true);
		this.userRepository.save(user);
	}

	// Helper method for authentication
	private Authentication authenticate(String username, String password) {
		return this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}

	// Overloaded authenticate method
	public User authenticate(User user) {
		this.authenticate(user.getUsername(), user.getPassword());

		return this.userRepository.findByUsername(user.getUsername()).map(UserService::isEmailVerified).get();
	}

	// Method to generate JWT headers
	public HttpHeaders generateJwtHeader(String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				this.jwtService.generateJwtToken(username, this.provider.getJwtExpiration()));

		return headers;
	}

	// New method to send reset password email
	public void sendResetPasswordEmail(String emailId) {
		Optional<User> opt = this.userRepository.findByEmailId(emailId);

		if (opt.isPresent()) {
			this.emailService.sendResetPasswordEmail(opt.get());
		} else {
			logger.debug("Email doesn't exist, {}", emailId);
		}
	}

	// New method to reset password
	public void resetPassword(String password) {
		// Retrieve the username of the logged-in user
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// Retrieve the user from the repository using the username
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		// Set the new encoded password
		user.setPassword(this.passwordEncoder.encode(password));

		// Save the updated user object
		this.userRepository.save(user);
	}

	// New method to get the authenticated user
	public User getUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		/* Get User from the DB. */
		return this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
	}

	// Define the updateValue method
	private void updateValue(Supplier<String> getter, Consumer<String> setter) {
		Optional.ofNullable(getter.get()).map(String::trim).ifPresent(setter);
	}

	// Define the updatePassword method
	private void updatePassword(Supplier<String> getter, Consumer<String> setter) {
		Optional.ofNullable(getter.get()).filter(StringUtils::hasText).map(this.passwordEncoder::encode)
				.ifPresent(setter);
	}

	// Define the updateUser method
	private User updateUser(User user, User currentUser) {
		this.updateValue(user::getFirstName, currentUser::setFirstName);
		this.updateValue(user::getLastName, currentUser::setLastName);
		this.updateValue(user::getPhone, currentUser::setPhone);
		this.updateValue(user::getEmailId, currentUser::setEmailId);
		this.updatePassword(user::getPassword, currentUser::setPassword);

		return this.userRepository.save(currentUser);
	}

	// Define the public updateUser method
	public User updateUser(User user) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// Validate email
		this.userRepository.findByEmailId(user.getEmailId()).filter(u -> !u.getUsername().equals(username))
				.ifPresent(u -> {
					throw new EmailExistException(String.format("Email already exists, %s", u.getEmailId()));
				});

		// Get and update user
		return this.userRepository.findByUsername(username).map(currentUser -> this.updateUser(user, currentUser))
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
	}

	// Define the updateUserProfile helper method
	private User updateUserProfile(Profile profile, User user) {
		Profile currentProfile = user.getProfile();

		if (Optional.ofNullable(currentProfile).isPresent()) {
			this.updateValue(profile::getHeadline, currentProfile::setHeadline);
			this.updateValue(profile::getBio, currentProfile::setBio);
			this.updateValue(profile::getCity, currentProfile::setCity);
			this.updateValue(profile::getCountry, currentProfile::setCountry);
			this.updateValue(profile::getPicture, currentProfile::setPicture);
		} else {
			user.setProfile(profile);
			profile.setUser(user);
		}

		return this.userRepository.save(user);
	}

	// Define the public updateUserProfile method
	public User updateUserProfile(Profile profile) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		return this.userRepository.findByUsername(username).map(user -> this.updateUserProfile(profile, user))
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
	}
}
