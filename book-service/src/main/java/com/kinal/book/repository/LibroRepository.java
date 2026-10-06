package com.kinal.book.repository;

import com.kinal.book.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    // Búsqueda dinámica con JPQL
    @Query("SELECT l FROM Libro l WHERE " +
           "(:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:autor IS NULL OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :autor, '%'))) AND " +
           "(:categoria IS NULL OR LOWER(l.categoria) LIKE LOWER(CONCAT('%', :categoria, '%'))) AND " +
           "(:isbn IS NULL OR l.isbn = :isbn)")
    Page<Libro> searchBooks(@Param("titulo") String titulo,
                            @Param("autor") String autor,
                            @Param("categoria") String categoria,
                            @Param("isbn") String isbn,
                            Pageable pageable);
}
