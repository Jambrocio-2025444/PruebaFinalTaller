package com.kinal.user.config;

import com.kinal.user.entity.EstadoUsuario;
import com.kinal.user.entity.RolUsuario;
import com.kinal.user.entity.Usuario;
import com.kinal.user.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByEmail("admin@biblioteca.com")) {
            Usuario admin = new Usuario(
                    "Administrador",
                    "admin@biblioteca.com",
                    passwordEncoder.encode("Admin123"),
                    EstadoUsuario.ACTIVO,
                    RolUsuario.ADMIN
            );
            usuarioRepository.save(admin);
            System.out.println("Usuario ADMIN creado por defecto: admin@biblioteca.com / Admin123");
        }
    }
}
