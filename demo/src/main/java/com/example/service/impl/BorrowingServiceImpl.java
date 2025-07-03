package com.example.service.impl;

import com.example.entity.Book;
import com.example.entity.BorrowedBook;
import com.example.entity.Member;
import com.example.exception.ValidationException;
import com.example.repository.BookRepository;
import com.example.repository.BorrowedBookRepository;
import com.example.repository.MemberRepository;
import com.example.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {
    private final BorrowedBookRepository borrowedBookRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Value("${library.max.books.per.member:10}")
    private int maxBooksPerMember;

    @Transactional
    @Override
    public BorrowedBook borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ValidationException("Учасника не знайдено"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ValidationException("Книгу не знайдено"));

        if (book.getCopiesAmount() <= 0) {
            throw new ValidationException("Книга недоступна для позичення");
        }

        long currentlyBorrowed = borrowedBookRepository.countByMemberId(memberId);
        if (currentlyBorrowed >= maxBooksPerMember) {
            throw new ValidationException("Перевищено ліміт позичених книг");
        }

        book.setCopiesAmount(book.getCopiesAmount() - 1);
        bookRepository.save(book);

        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setMember(member);
        borrowedBook.setBook(book);

        return borrowedBookRepository.save(borrowedBook);
    }

    @Transactional
    @Override
    public void returnBook(Long borrowingId) {
        BorrowedBook borrowedBook = borrowedBookRepository.findById(borrowingId)
                .orElseThrow(() -> new ValidationException("Запис про позичення не знайдено"));

        Book book = borrowedBook.getBook();
        book.setCopiesAmount(book.getCopiesAmount() + 1);
        bookRepository.save(book);

        borrowedBookRepository.delete(borrowedBook);
    }

    @Override
    public List<BorrowedBook> getBorrowedBooksByMemberName(String memberName) {
        return borrowedBookRepository.findByMemberName(memberName);
    }

    @Override
    public List<String> getAllBorrowedBookNames() {
        return borrowedBookRepository.findDistinctBookNames();
    }

    @Override
    public List<Map<String, Object>> getBorrowedBooksWithCount() {
        return borrowedBookRepository.findBooksWithBorrowCount();
    }
}
