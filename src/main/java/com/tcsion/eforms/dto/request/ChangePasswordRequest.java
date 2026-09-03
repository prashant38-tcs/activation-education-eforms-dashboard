package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ChangePasswordRequest {
    private String currentPassword;
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
    @NotBlank(message = "Please confirm the new password")
    private String confirmPassword;
}
