package com.kinal.loan.service;

import com.kinal.loan.client.ExternalClients;
import com.kinal.loan.dto.request.PrestamoRequest;
import com.kinal.loan.dto.response.PrestamoResponse;
import com.kinal.loan.entity.EstadoPrestamo;
import com.kinal.loan.entity.Prestamo;
import com.kinal.loan.repository.PrestamoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final PrestamoRepository prestamoRepository;
    private final ExternalClients externalClients;

    public LoanService(PrestamoRepository prestamoRepository, ExternalClients externalClients) {
        this.prestamoRepository = prestamoRepository;
        this.externalClients = externalClients;
    }

    @Transactional
    public PrestamoResponse crearPrestamo(PrestamoRequest request) {
        Long uId = request.usuarioId();
        
        // REGLA 5: Si tiene préstamos atrasados -> Sancionar y rechazar
        if (prestamoRepository.existsByUsuarioIdAndEstado(uId, EstadoPrestamo.ATRASADO)) {
            externalClients.sancionarUsuario(uId);
            throw new RuntimeException("El usuario tiene préstamos atrasados. Ha sido sancionado y no puede realizar nuevos préstamos.");
        }

        // REGLA 2: Límite de 3 préstamos
        long activos = prestamoRepository.countByUsuarioIdAndEstado(uId, EstadoPrestamo.ACTIVO);
        if (activos >= 3) {
            throw new RuntimeException("El usuario ya alcanzó el límite de 3 préstamos activos simultáneos.");
        }

        // REGLA 1 y 6: Disminuir stock en book-service
        externalClients.reducirStock(request.libroId());

        // REGLA 3: Plazo de 14 días
        try {
            Prestamo prestamo = new Prestamo(
                    uId,
                    request.libroId(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(14)
            );
            return mapToResponse(prestamoRepository.save(prestamo));
        } catch (Exception e) {
            // Compensación si falla el guardado local después de reducir stock
            externalClients.aumentarStock(request.libroId());
            throw new RuntimeException("Error interno al registrar el préstamo, transacción cancelada.");
        }
    }

    @Transactional
    public void devolverPrestamo(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // REGLA 8: No devolver dos veces
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new RuntimeException("El préstamo ya se encuentra devuelto.");
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamoRepository.save(prestamo);

        // REGLA 7: Aumentar stock
        externalClients.aumentarStock(prestamo.getLibroId());
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> getMisPrestamos(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> getPrestamosAtrasados() {
        return prestamoRepository.findByEstado(EstadoPrestamo.ATRASADO).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Tarea programada: REGLA 4 (Si no se devuelve en la fecha -> ATRASADO)
    // Se ejecuta cada día a medianoche (00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void actualizarPrestamosAtrasados() {
        List<Prestamo> activos = prestamoRepository.findByEstadoAndFechaDevolucionEsperadaBefore(
                EstadoPrestamo.ACTIVO, LocalDate.now());
                
        for (Prestamo p : activos) {
            p.setEstado(EstadoPrestamo.ATRASADO);
        }
        prestamoRepository.saveAll(activos);
    }

    private PrestamoResponse mapToResponse(Prestamo p) {
        return new PrestamoResponse(
                p.getId(), p.getUsuarioId(), p.getLibroId(),
                p.getFechaPrestamo(), p.getFechaDevolucionEsperada(),
                p.getFechaDevolucionReal(), p.getEstado().name()
        );
    }
}
