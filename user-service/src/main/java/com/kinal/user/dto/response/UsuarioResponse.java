package com.kinal.user.dto.response;

import com.kinal.user.entity.EstadoUsuario;
import com.kinal.user.entity.RolUsuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        EstadoUsuario estado,
        RolUsuario rol
) {}
