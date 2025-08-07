package com.library.management.controller;

import com.library.management.dto.request.BookCreateRequest;
import com.library.management.dto.request.BookUpdateRequest;
import com.library.management.dto.response.BookResponse;
import com.library.management.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books in the library")
public class BookController {

    private final BookService bookService;

    @PostMapping("/create")
    @Operation(summary = "Add a new book", description = "Adds a new book to the library (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Book with ISBN already exists")
    })
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request) {
        BookResponse response = bookService.createBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/is_available")
    @Operation(summary = "Check book availability", description = "Checks if a book is available for borrowing (User/Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book availability checked successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Boolean> isBookAvailable(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        Boolean isAvailable = bookService.isBookAvailable(id);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/retrieve/{id}")
    @Operation(summary = "Retrieve book details", description = "Retrieves detailed information about a book (User/Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponse> retrieveBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        BookResponse response = bookService.getBookById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "Get all books", description = "Retrieves all books with pagination and optional filtering (User/Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully")
    })
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @Parameter(description = "Category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Availability filter") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<BookResponse> response = bookService.getAllBooks(categoryId, available, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by title, author, or ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @Parameter(description = "Search query (title, author, or ISBN)") @RequestParam String query,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<BookResponse> response = bookService.searchBooks(query, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get books by category", description = "Retrieves all books in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<BookResponse>> getBooksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<BookResponse> response = bookService.getBooksByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available books", description = "Retrieves all books that are currently available for borrowing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available books retrieved successfully")
    })
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<BookResponse> response = bookService.getAvailableBooks(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular-books")
    @Operation(summary = "Get popular books", description = "Retrieves the most borrowed books (User)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popular books retrieved successfully")
    })
    public ResponseEntity<List<BookResponse>> getPopularBooks(
            @Parameter(description = "Number of books to return") @RequestParam(defaultValue = "10") int limit) {
        List<BookResponse> response = bookService.getPopularBooks(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended-books")
    @Operation(summary = "Get recommended books", description = "Retrieves recommended books based on user preferences (User)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommended books retrieved successfully")
    })
    public ResponseEntity<List<BookResponse>> getRecommendedBooks(
            @Parameter(description = "Number of books to return") @RequestParam(defaultValue = "10") int limit) {
        List<BookResponse> response = bookService.getRecommendedBooks(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new-collection")
    @Operation(summary = "Get new collection", description = "Retrieves newly added books (User)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New collection retrieved successfully")
    })
    public ResponseEntity<List<BookResponse>> getNewCollection(
            @Parameter(description = "Number of books to return") @RequestParam(defaultValue = "10") int limit) {
        List<BookResponse> response = bookService.getNewBooks(limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Update book", description = "Updates an existing book (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Book or category not found"),
            @ApiResponse(responseCode = "409", description = "ISBN already exists for another book")
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete book", description = "Deletes a book if it has no active borrows (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "Book has active borrows")
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Update book availability", description = "Updates the available copies of a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book availability updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid availability count"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponse> updateBookAvailability(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "New available copies count") @RequestParam int availableCopies) {
        BookResponse response = bookService.updateBookAvailability(id, availableCopies);
        return ResponseEntity.ok(response);
    }
//    test
}

