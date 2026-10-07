package com.kinal.auth.service;

import com.kinal.auth.dto.request.LoginRequest;
import com.kinal.auth.dto.request.RegisterRequest;
import com.kinal.auth.dto.response.AuthResponse;
import com.kinal.auth.dto.response.UserInternalResponse;
import com.kinal.auth.exception.BusinessRuleException;
import com.kinal.auth.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient;

    public AuthService(JwtProvider jwtProvider,
                       PasswordEncoder passwordEncoder,
                       @Value("${application.user-service.url}") String userServiceUrl) {
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Llamar al user-service para obtener datos del usuario
        UserInternalResponse user = fetchUserFromUserService(request.email());

        // 2. Verificar contraseña con BCrypt
        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // 3. Generar JWT
        String token = jwtProvider.generateToken(user);

        return new AuthResponse(token, "Bearer");
    }

    public String register(RegisterRequest request) {
        return restClient.post()
                .uri("/api/v1/usuarios/internal/register")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new BusinessRuleException("Error al registrar: verifique los datos");
                })
                .body(String.class);
    }

    private UserInternalResponse fetchUserFromUserService(String email) {
        return restClient.get()
                .uri("/api/v1/usuarios/internal/{email}", email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new BadCredentialsException("Credenciales inválidas");
                })
                .body(UserInternalResponse.class);
    }
}