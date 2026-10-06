package com.kinal.user.service;

import com.kinal.user.dto.request.RegisterInternalRequest;
import com.kinal.user.dto.request.UsuarioRequest;
import com.kinal.user.dto.response.UserInternalResponse;
import com.kinal.user.dto.response.UsuarioResponse;
import com.kinal.user.entity.EstadoUsuario;
import com.kinal.user.entity.RolUsuario;
import com.kinal.user.entity.Usuario;
import com.kinal.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> getAllUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapToResponse(usuario);
    }

    @Transactional
    public UsuarioResponse createUsuario(UsuarioRequest request) {
        validateEmail(request.email());
        Usuario usuario = new Usuario(
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                EstadoUsuario.ACTIVO,
                request.rol()
        );
        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserInternalResponse getInternalByEmail(String email) {
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return new UserInternalResponse(
                user.getId(), user.getEmail(), user.getPassword(), 
                user.getRol().name(), user.getEstado().name()
        );
    }

    @Transactional
    public String registerInternal(RegisterInternalRequest request) {
        validateEmail(request.email());
        Usuario usuario = new Usuario(
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                EstadoUsuario.ACTIVO,
                RolUsuario.LECTOR 
        );
        usuarioRepository.save(usuario);
        return "Usuario registrado exitosamente";
    }

    @Transactional
    public void sancionarUsuario(Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setEstado(EstadoUsuario.SANCIONADO);
        usuarioRepository.save(user);
    }

    private void validateEmail(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }
    }

    private UsuarioResponse mapToResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getEstado(), u.getRol());
    }
}
