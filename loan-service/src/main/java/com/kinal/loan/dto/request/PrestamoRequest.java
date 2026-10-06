package com.kinal.loan.dto.request;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequest(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long usuarioId,

        @NotNull(message = "El ID del libro es obligatorio")
        Long libroId
) {}
