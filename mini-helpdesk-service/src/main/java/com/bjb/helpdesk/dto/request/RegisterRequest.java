package com.bjb.helpdesk.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank
    private String username;
    @Email @NotBlank
    private String email;
    @NotBlank @Size(min = 6)
    private String password;
}
