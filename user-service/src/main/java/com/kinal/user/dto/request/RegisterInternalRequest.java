package com.kinal.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterInternalRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank String password
) {}
