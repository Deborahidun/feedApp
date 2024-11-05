package com.bptn.feedApp.service;

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
}
