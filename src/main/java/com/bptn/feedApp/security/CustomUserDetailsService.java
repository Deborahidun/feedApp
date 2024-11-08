package com.bptn.feedApp.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bptn.feedApp.jpa.User;
import com.bptn.feedApp.repository.UserRepository;

@Service("UserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// Retrieve user by username using the UserRepository
		Optional<User> opt = this.userRepository.findByUsername(username);

		// Throw exception if user is not found
		if (opt.isEmpty()) {
			throw new UsernameNotFoundException("Username not found: " + username);
		}

		// Return a CustomUserDetails object if user is found
		return new CustomUserDetails(opt.get());
	}
}
