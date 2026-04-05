package ch.uzh.ifi.hase.soprafs26.rest.dto;

import org.hibernate.annotations.processing.Pattern;

import jakarta.validation.constraints.*;

public class UserPostDTO {

	@NotBlank(message = "Username can't be blank")
	@Size(min = 2, message = "Username must be at least 2 characters long")
	private String username;

	@NotBlank(message = "Email can't be blank")
	@Email(message = "Email should be a valid email")
	private String email;

	@NotBlank(message = "Password can't be blank")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
