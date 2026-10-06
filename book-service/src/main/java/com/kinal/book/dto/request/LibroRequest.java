package com.kinal.book.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LibroRequest(
        @NotBlank(message = "El ISBN es obligatorio")
        String isbn,

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        String autor,

        @NotBlank(message = "La categoría es obligatoria")
        String categoria,

        @NotNull(message = "El stock total es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stockTotal
) {}
