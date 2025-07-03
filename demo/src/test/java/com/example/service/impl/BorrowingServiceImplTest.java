package com.example.service.impl;

import com.example.entity.Book;
import com.example.entity.BorrowedBook;
import com.example.entity.Member;
import com.example.exception.ValidationException;
import com.example.repository.BookRepository;
import com.example.repository.BorrowedBookRepository;
import com.example.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowingServiceImplTest {

    @Mock
    private BorrowedBookRepository borrowedBookRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Встановлюємо ліміт на кількість книг, що можна позичити
        ReflectionTestUtils.setField(borrowingService, "maxBooksPerMember", 10);
    }

    // 1. borrowBook

    @Test
    void borrowBook_successfulBorrowing() {
        Long memberId = 1L;
        Long bookId = 2L;

        Member member = new Member();
        member.setId(memberId);

        Book book = new Book();
        book.setId(bookId);
        book.setCopiesAmount(5);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowedBookRepository.countByMemberId(memberId)).thenReturn(3L);

        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setMember(member);
        borrowedBook.setBook(book);

        when(borrowedBookRepository.save(any(BorrowedBook.class))).thenReturn(borrowedBook);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        BorrowedBook result = borrowingService.borrowBook(memberId, bookId);

        assertEquals(member, result.getMember());
        assertEquals(book, result.getBook());
        assertEquals(4, book.getCopiesAmount()); // copiesAmount зменшився на 1

        verify(bookRepository).save(book);
        verify(borrowedBookRepository).save(any(BorrowedBook.class));
    }

    @Test
    void borrowBook_memberNotFound_throwsValidationException() {
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> borrowingService.borrowBook(1L, 1L));
        assertEquals("Учасника не знайдено", ex.getMessage());
    }

    @Test
    void borrowBook_bookNotFound_throwsValidationException() {
        Member member = new Member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> borrowingService.borrowBook(1L, 1L));
        assertEquals("Книгу не знайдено", ex.getMessage());
    }

    @Test
    void borrowBook_noCopiesAvailable_throwsValidationException() {
        Member member = new Member();
        Book book = new Book();
        book.setCopiesAmount(0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> borrowingService.borrowBook(1L, 1L));
        assertEquals("Книга недоступна для позичення", ex.getMessage());
    }

    @Test
    void borrowBook_exceedsLimit_throwsValidationException() {
        Member member = new Member();
        Book book = new Book();
        book.setCopiesAmount(2);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowedBookRepository.countByMemberId(1L)).thenReturn(10L);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> borrowingService.borrowBook(1L, 1L));
        assertEquals("Перевищено ліміт позичених книг", ex.getMessage());
    }

    // 2. returnBook

    @Test
    void returnBook_successfulReturn() {
        Long borrowingId = 1L;

        Book book = new Book();
        book.setCopiesAmount(2);

        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setId(borrowingId);
        borrowedBook.setBook(book);

        when(borrowedBookRepository.findById(borrowingId)).thenReturn(Optional.of(borrowedBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        borrowingService.returnBook(borrowingId);

        assertEquals(3, book.getCopiesAmount()); // copiesAmount збільшився на 1

        verify(bookRepository).save(book);
        verify(borrowedBookRepository).delete(borrowedBook);
    }

    @Test
    void returnBook_borrowingNotFound_throwsValidationException() {
        when(borrowedBookRepository.findById(1L)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> borrowingService.returnBook(1L));
        assertEquals("Запис про позичення не знайдено", ex.getMessage());
    }

    // 3. getBorrowedBooksByMemberName

    @Test
    void getBorrowedBooksByMemberName_returnsList() {
        String memberName = "John";

        List<BorrowedBook> borrowedBooks = List.of(new BorrowedBook(), new BorrowedBook());

        when(borrowedBookRepository.findByMemberName(memberName)).thenReturn(borrowedBooks);

        List<BorrowedBook> result = borrowingService.getBorrowedBooksByMemberName(memberName);

        assertEquals(2, result.size());
        verify(borrowedBookRepository).findByMemberName(memberName);
    }

    @Test
    void getBorrowedBooksByMemberName_emptyList() {
        String memberName = "Unknown";

        when(borrowedBookRepository.findByMemberName(memberName)).thenReturn(List.of());

        List<BorrowedBook> result = borrowingService.getBorrowedBooksByMemberName(memberName);

        assertTrue(result.isEmpty());
    }

    // 4. getAllBorrowedBookNames

    @Test
    void getAllBorrowedBookNames_returnsList() {
        List<String> names = List.of("Book1", "Book2");

        when(borrowedBookRepository.findDistinctBookNames()).thenReturn(names);

        List<String> result = borrowingService.getAllBorrowedBookNames();

        assertEquals(2, result.size());
        verify(borrowedBookRepository).findDistinctBookNames();
    }

    @Test
    void getAllBorrowedBookNames_emptyList() {
        when(borrowedBookRepository.findDistinctBookNames()).thenReturn(List.of());

        List<String> result = borrowingService.getAllBorrowedBookNames();

        assertTrue(result.isEmpty());
    }

    // 5. getBorrowedBooksWithCount

    @Test
    void getBorrowedBooksWithCount_returnsList() {
        List<Map<String, Object>> data = List.of(
                Map.of("bookName", "Book1", "borrowCount", 5),
                Map.of("bookName", "Book2", "borrowCount", 3)
        );

        when(borrowedBookRepository.findBooksWithBorrowCount()).thenReturn(data);

        List<Map<String, Object>> result = borrowingService.getBorrowedBooksWithCount();

        assertEquals(2, result.size());
        verify(borrowedBookRepository).findBooksWithBorrowCount();
    }

    @Test
    void getBorrowedBooksWithCount_emptyList() {
        when(borrowedBookRepository.findBooksWithBorrowCount()).thenReturn(List.of());

        List<Map<String, Object>> result = borrowingService.getBorrowedBooksWithCount();

        assertTrue(result.isEmpty());
    }
}
