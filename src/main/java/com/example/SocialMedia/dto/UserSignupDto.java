package com.example.SocialMedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupDto {
    @NotBlank(message = "username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]$", message = "Username must be 3-30 characters, start with a letter, and contain only letters, numbers, underscores, periods, or hyphens")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "email cannot be blank")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Invalid email address")
    private String email;

    @NotBlank(message = "password cannot be empty")
    @Pattern(regexp ="^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$" , message ="Password must be at least 6 characters and should contain at least 1 number")
    @Size(min = 6, max = 15)
    private String password;
}
