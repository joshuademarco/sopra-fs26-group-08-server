package ch.uzh.ifi.hase.soprafs26.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileDTO {

    @NotBlank(message = "Username can't be blank")
    @Size(min = 2, message = "Username must be at least 2 characters long")
    @Pattern(regexp = "^\\S+$", message = "Username must not contain any spaces")
    private String username;

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Email should be a valid email")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
