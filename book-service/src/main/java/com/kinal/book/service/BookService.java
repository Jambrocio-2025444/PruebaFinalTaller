package com.kinal.book.service;

import com.kinal.book.dto.request.LibroRequest;
import com.kinal.book.dto.response.LibroResponse;
import com.kinal.book.entity.Libro;
import com.kinal.book.repository.LibroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final LibroRepository libroRepository;

    public BookService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public Page<LibroResponse> searchBooks(String titulo, String autor, String categoria, String isbn, Pageable pageable) {
        return libroRepository.searchBooks(titulo, autor, categoria, isbn, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LibroResponse getBookById(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
        return mapToResponse(libro);
    }

    @Transactional
    public LibroResponse createBook(LibroRequest request) {
        if (libroRepository.existsByIsbn(request.isbn())) {
            throw new RuntimeException("El ISBN ya está registrado");
        }
        
        Libro libro = new Libro(
                request.isbn(),
                request.titulo(),
                request.autor(),
                request.categoria(),
                request.stockTotal(),
                request.stockTotal() // Al crearse, el stock disponible es igual al total
        );
        return mapToResponse(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse updateBook(Long id, LibroRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
                
        // Validar ISBN único si cambia
        if (!libro.getIsbn().equals(request.isbn()) && libroRepository.existsByIsbn(request.isbn())) {
            throw new RuntimeException("El nuevo ISBN ya pertenece a otro libro");
        }

        // Calcular ajuste de stock
        int diferenciaStock = request.stockTotal() - libro.getStockTotal();
        int nuevoStockDisponible = libro.getStockDisponible() + diferenciaStock;
        
        if (nuevoStockDisponible < 0) {
            throw new RuntimeException("No se puede reducir el stock a un valor que dejaría el stock disponible en negativo");
        }

        libro.setIsbn(request.isbn());
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setCategoria(request.categoria());
        libro.setStockTotal(request.stockTotal());
        libro.setStockDisponible(nuevoStockDisponible);

        return mapToResponse(libroRepository.save(libro));
    }

    @Transactional
    public void deleteBook(Long id) {
        libroRepository.deleteById(id);
    }

    // --- MÉTODOS INTERNOS PARA TRANSACCIONES CON LOAN-SERVICE ---

    @Transactional
    public void reducirStock(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getStockDisponible() <= 0) {
            throw new RuntimeException("No hay stock disponible para este libro");
        }

        libro.setStockDisponible(libro.getStockDisponible() - 1);
        libroRepository.save(libro); // Aquí actúa el @Version automáticamente
    }

    @Transactional
    public void aumentarStock(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getStockDisponible() >= libro.getStockTotal()) {
            throw new RuntimeException("Error lógico: El stock disponible superaría al total");
        }

        libro.setStockDisponible(libro.getStockDisponible() + 1);
        libroRepository.save(libro);
    }

    private LibroResponse mapToResponse(Libro l) {
        return new LibroResponse(
                l.getId(), l.getIsbn(), l.getTitulo(), 
                l.getAutor(), l.getCategoria(), 
                l.getStockTotal(), l.getStockDisponible()
        );
    }
}
