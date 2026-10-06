package com.kinal.book.controller;

import com.kinal.book.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/libros/internal")
public class BookInternalController {

    private final BookService bookService;

    public BookInternalController(BookService bookService) {
        this.bookService = bookService;
    }

    @PutMapping("/{id}/reducir-stock")
    public ResponseEntity<Void> reducirStock(@PathVariable Long id) {
        bookService.reducirStock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/aumentar-stock")
    public ResponseEntity<Void> aumentarStock(@PathVariable Long id) {
        bookService.aumentarStock(id);
        return ResponseEntity.noContent().build();
    }
}
