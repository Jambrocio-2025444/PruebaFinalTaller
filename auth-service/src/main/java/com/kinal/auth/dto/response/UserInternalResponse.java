package com.kinal.auth.dto.response;

public record UserInternalResponse(
        Long id,
        String email,
        String password,
        String rol,
        String estado
) {}
