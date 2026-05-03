package ch.uzh.ifi.hase.soprafs26.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {

    @NotBlank(message = "Current password can't be blank")
    private String currentPassword;

    @NotBlank(message = "New password can't be blank")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    @Pattern(regexp = ".*[0-9].*", message = "New password must contain at least one number")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
