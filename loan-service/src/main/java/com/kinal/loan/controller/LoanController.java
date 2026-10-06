package com.kinal.loan.controller;

import com.kinal.loan.dto.request.PrestamoRequest;
import com.kinal.loan.dto.response.PrestamoResponse;
import com.kinal.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prestamos")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<PrestamoResponse> crearPrestamo(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse response = loanService.crearPrestamo(request);
        return ResponseEntity.status(201).body(response);
    }

    @PatchMapping("/{id}/devolucion")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Void> devolverPrestamo(@PathVariable Long id) {
        loanService.devolverPrestamo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mis-prestamos")
    @PreAuthorize("hasAuthority('LECTOR')")
    public ResponseEntity<List<PrestamoResponse>> getMisPrestamos(Authentication authentication) {
        // Extraemos el userId de los detalles configurados en el filtro JWT
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(loanService.getMisPrestamos(userId));
    }

    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<List<PrestamoResponse>> getPrestamosAtrasados() {
        return ResponseEntity.ok(loanService.getPrestamosAtrasados());
    }
}
