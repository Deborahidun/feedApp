package com.bptn.feedApp.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bptn.feedApp.jpa.User;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	// Field for the User entity
	private User user;

	// Constructor using the User field
	public CustomUserDetails(User user) {
		super();
		this.user = user;
	}

	// Method to get user authorities (returning null as per instruction)
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	// Method to get the user's password
	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	// Method to get the user's username
	@Override
	public String getUsername() {
		return this.user.getUsername();
	}

	// Method to check if the account is non-expired (always true)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// Method to check if the account is non-locked (always true)
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// Method to check if the credentials are non-expired (always true)
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// Method to check if the account is enabled (always true)
	@Override
	public boolean isEnabled() {
		return true;
	}
}
