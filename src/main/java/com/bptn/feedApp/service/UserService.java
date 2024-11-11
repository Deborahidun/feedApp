package com.bptn.feedApp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bptn.feedApp.exception.domain.EmailExistException;
import com.bptn.feedApp.exception.domain.UsernameExistException;
import com.bptn.feedApp.jpa.User;
import com.bptn.feedApp.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService emailService; // Autowire EmailService

	@Autowired
	PasswordEncoder passwordEncoder; // Autowire PasswordEncoder

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
		// Convert username and emailId to lowercase
		user.setUsername(user.getUsername().toLowerCase());
		user.setEmailId(user.getEmailId().toLowerCase());

		// Validate username and email for duplication
		this.validateUsernameAndEmail(user.getUsername(), user.getEmailId());

		// Set emailVerified to false and encrypt the password
		user.setEmailVerified(false);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		user.setCreatedOn(Timestamp.from(Instant.now()));

		// Save the user to the database
		this.userRepository.save(user);

		// Send the verification email after saving the user
		this.emailService.sendVerificationEmail(user);

		// Return the saved user object
		return user;
	}
}
