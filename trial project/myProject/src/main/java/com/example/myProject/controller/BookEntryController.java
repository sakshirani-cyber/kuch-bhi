package com.example.myProject.controller;

import com.example.myProject.entity.BookEntry;
import com.example.myProject.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/books")
public class BookEntryController {

    private final Map<Long, BookEntry> bookEntries = new HashMap<>();

    @GetMapping
    public ResponseEntity<List<BookEntry>> getAll() {
        return ResponseEntity.ok(new ArrayList<>(bookEntries.values()));
    }

    @PostMapping
    public ResponseEntity<BookEntry> createEntry(@RequestBody BookEntry entry) {
        if (entry == null || entry.getId() <= 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Book create failed",
                    Map.of("id", "Valid book id is required")
            );
        }
        if (entry.getBookTitle() == null || entry.getBookTitle().isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Book create failed",
                    Map.of("bookTitle", "Book title is mandatory")
            );
        }
        bookEntries.put(entry.getId(), entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookEntry> getBookById(@PathVariable Long id) {
        BookEntry entry = bookEntries.get(id);
        if (entry == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Book not found",
                    Map.of("id", "No book exists with id " + id)
            );
        }
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBookById(@PathVariable Long id) {
        BookEntry removed = bookEntries.remove(id);
        if (removed == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Book not found",
                    Map.of("id", "No book exists with id " + id)
            );
        }
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
    }
}
