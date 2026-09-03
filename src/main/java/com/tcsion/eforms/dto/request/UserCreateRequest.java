package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Getter
@Setter
public class UserCreateRequest {
    private String employeeCode;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @Email(message = "A valid email address is required")
    private String email;
    @NotEmpty(message = "At least one role must be assigned")
    private Set<String> roleCodes;
}
