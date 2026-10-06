package com.kinal.loan.dto.response;

import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        Long usuarioId,
        Long libroId,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionEsperada,
        LocalDate fechaDevolucionReal,
        String estado
) {}
