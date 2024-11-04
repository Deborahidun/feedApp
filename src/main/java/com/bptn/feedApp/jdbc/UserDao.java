package com.bptn.feedApp.jdbc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	JdbcTemplate jdbcTemplate;

	public List<UserBean> listUsers() {
		String sql = "SELECT * FROM \"user\"";
		return this.jdbcTemplate.query(sql, new UserMapper());
	}

	public UserBean findByUsername(String username) {
		String sql = "SELECT * FROM \"user\" WHERE username = ?";
		List<UserBean> users = this.jdbcTemplate.query(sql, new UserMapper(), username);

		return users.isEmpty() ? null : users.get(0);
	}

	public void createUser(UserBean user) {
		String sql = "INSERT INTO \"user\" (\"firstName\", \"lastName\",\"username\", \"phone\", \"emailId\", \"password\", \"emailVerified\", \"createdOn\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		logger.debug("Insert Query: {}", sql);

		this.jdbcTemplate.update(sql, new Object[] { user.getFirstName(), user.getLastName(), user.getUsername(),
				user.getPhone(), user.getEmailId(), user.getPassword(), user.getEmailVerified(), user.getCreatedOn() });
	}
}
