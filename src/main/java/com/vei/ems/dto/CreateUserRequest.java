package com.vei.ems.dto;

import com.vei.ems.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "Username must be 3-20 letters, digits or underscore")
        String username,
        @NotBlank(message = "Password is required") String password,
        @NotNull(message = "Role is required") Role role) {
}
