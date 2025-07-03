
package com.example.controller;

import com.example.entity.BorrowedBook;
import com.example.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Позичення", description = "API для управління позиченнями книг")
public class BorrowingController {
    private final BorrowingService borrowingService;

    @Operation(summary = "Позичити книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу успішно позичено"),
            @ApiResponse(responseCode = "400", description = "Неможливо позичити книгу"),
            @ApiResponse(responseCode = "404", description = "Книгу або користувача не знайдено")
    })
    @PostMapping("/borrow")
    public ResponseEntity<BorrowedBook> borrowBook(
            @Parameter(description = "ID користувача") @RequestParam Long memberId,
            @Parameter(description = "ID книги") @RequestParam Long bookId
    ) {
        return ResponseEntity.ok(borrowingService.borrowBook(memberId, bookId));
    }

    @Operation(summary = "Повернути книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу успішно повернено"),
            @ApiResponse(responseCode = "404", description = "Запис про позичення не знайдено")
    })
    @PostMapping("/return/{borrowingId}")
    public ResponseEntity<Void> returnBook(
            @Parameter(description = "ID позичення") @PathVariable Long borrowingId
    ) {
        borrowingService.returnBook(borrowingId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отримати список позичених книг користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список позичених книг отримано"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @GetMapping("/member/{memberName}")
    public ResponseEntity<List<BorrowedBook>> getBorrowedBooksByMember(
            @Parameter(description = "Ім'я користувача") @PathVariable String memberName
    ) {
        return ResponseEntity.ok(borrowingService.getBorrowedBooksByMemberName(memberName));
    }

    @Operation(summary = "Отримати список унікальних назв позичених книг")
    @ApiResponse(responseCode = "200", description = "Список назв отримано")
    @GetMapping("/books/distinct")
    public ResponseEntity<List<String>> getDistinctBorrowedBooks() {
        return ResponseEntity.ok(borrowingService.getAllBorrowedBookNames());
    }

    @Operation(summary = "Отримати статистику позичень книг")
    @ApiResponse(responseCode = "200", description = "Статистику отримано")
    @GetMapping("/books/statistics")
    public ResponseEntity<List<Map<String, Object>>> getBorrowedBooksWithCount() {
        return ResponseEntity.ok(borrowingService.getBorrowedBooksWithCount());
    }
}
