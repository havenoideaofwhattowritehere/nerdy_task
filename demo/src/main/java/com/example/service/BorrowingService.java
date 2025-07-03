package com.example.service;

import com.example.entity.BorrowedBook;

import java.util.List;
import java.util.Map;

public interface BorrowingService {
    BorrowedBook borrowBook(Long memberId, Long bookId);
    void returnBook(Long borrowingId);
    List<BorrowedBook> getBorrowedBooksByMemberName(String memberName);
    List<String> getAllBorrowedBookNames();
    List<Map<String, Object>> getBorrowedBooksWithCount();

}
