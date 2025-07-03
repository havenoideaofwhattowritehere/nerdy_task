package com.example.controller;

import com.example.entity.Book;
import com.example.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Книги", description = "API для управління книгами")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "Створити нову книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Книгу успішно створено"),
            @ApiResponse(responseCode = "400", description = "Неправильні вхідні дані")
    })
    @PostMapping
    public ResponseEntity<Book> createBook(
            @Parameter(description = "Дані книги") @RequestBody Book book
    ) {
        return new ResponseEntity<>(bookService.createBook(book), HttpStatus.CREATED);
    }

    @Operation(summary = "Отримати всі книги")
    @ApiResponse(responseCode = "200", description = "Список всіх книг успішно отримано")
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Отримати книгу за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу знайдено"),
            @ApiResponse(responseCode = "404", description = "Книгу не знайдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(
            @Parameter(description = "ID книги") @PathVariable Long id
    ) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Знайти книгу за назвою")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу знайдено"),
            @ApiResponse(responseCode = "404", description = "Книгу не знайдено")
    })
    @GetMapping("/title")
    public ResponseEntity<?> findByTitle(
            @Parameter(description = "Назва книги") @RequestParam String title
    ) {
        Optional<Book> book = bookService.findByTitle(title);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Знайти книги за автором")
    @ApiResponse(responseCode = "200", description = "Список книг автора")
    @GetMapping("/author")
    public ResponseEntity<List<Book>> findByAuthor(
            @Parameter(description = "Ім'я автора") @RequestParam String author
    ) {
        return ResponseEntity.ok(bookService.findByAuthor(author));
    }

    @Operation(summary = "Оновити інформацію про книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу оновлено"),
            @ApiResponse(responseCode = "404", description = "Книгу не знайдено")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Оновлені дані книги") @RequestBody Book book
    ) {
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    @Operation(summary = "Видалити книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Книгу видалено"),
            @ApiResponse(responseCode = "404", description = "Книгу не знайдено")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID книги") @PathVariable Long id
    ) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пошук книги за назвою та автором")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу знайдено"),
            @ApiResponse(responseCode = "404", description = "Книгу не знайдено")
    })
    @GetMapping("/search")
    public ResponseEntity<?> findByTitleAndAuthor(
            @Parameter(description = "Назва книги") @RequestParam String title,
            @Parameter(description = "Автор книги") @RequestParam String author
    ) {
        Optional<Book> book = bookService.findByTitleAndAuthor(title, author);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
