package com.kinal.auth.dto.response;

public record AuthResponse(
        String token,
        String type
) {}
