package com.kinal.loan.repository;

import com.kinal.loan.entity.EstadoPrestamo;
import com.kinal.loan.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);
    boolean existsByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);
    List<Prestamo> findByUsuarioId(Long usuarioId);
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    List<Prestamo> findByEstadoAndFechaDevolucionEsperadaBefore(EstadoPrestamo estado, LocalDate date);
}
