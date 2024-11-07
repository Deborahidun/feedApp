package com.bptn.feedApp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bptn.feedApp.jpa.User;
import com.bptn.feedApp.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService emailService; // Autowire EmailService

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

	// Method to sign up a new user
	public User signup(User user) {
		// Convert username and emailId to lowercase
		user.setUsername(user.getUsername().toLowerCase());
		user.setEmailId(user.getEmailId().toLowerCase());

		// Set emailVerified to false
		user.setEmailVerified(false);

		// Set createdOn to the current timestamp
		user.setCreatedOn(Timestamp.from(Instant.now()));

		// Save the user to the database
		this.userRepository.save(user);

		// Send the verification email after saving the user
		this.emailService.sendVerificationEmail(user);

		// Return the saved user object
		return user;
	}
}
