package com.example.service.impl;

import com.example.entity.Book;
import com.example.exception.ValidationException;
import com.example.repository.BookRepository;
import com.example.validator.BookValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookValidator bookValidator;

    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. createBook

    @Test
    void createBook_newBook_savesWithCopiesAmountOne() {
        Book book = new Book(null, " Title ", " Author ", 0);

        // Важливо: мок на supports()
        when(bookValidator.supports(Book.class)).thenReturn(true);

        doNothing().when(bookValidator).validate(any(), any());

        when(bookRepository.findByTitleAndAuthor("Title", "Author")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.createBook(book);

        assertEquals("Title", result.getTitle());
        assertEquals("Author", result.getAuthor());
        assertEquals(1, result.getCopiesAmount());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_existingBook_increasesCopiesAmount() {
        Book book = new Book(null, "Title", "Author", 0);
        Book existing = new Book(1L, "Title", "Author", 3);

        when(bookValidator.supports(Book.class)).thenReturn(true);

        doNothing().when(bookValidator).validate(any(), any());

        when(bookRepository.findByTitleAndAuthor("Title", "Author")).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        Book result = bookService.createBook(book);

        assertEquals(4, result.getCopiesAmount());
        verify(bookRepository).save(existing);
    }


    @Test
    void createBook_validationFails_throwsValidationException() {
        Book book = new Book(null, "", "", 0);

        when(bookValidator.supports(Book.class)).thenReturn(true);

        doAnswer(invocation -> {
            Object target = invocation.getArgument(0);

            org.springframework.validation.Errors errors = invocation.getArgument(1);
            errors.reject("error", "Invalid data");

            return null;
        }).when(bookValidator).validate(any(), any());

        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.createBook(book));
        assertTrue(ex.getMessage().contains("Invalid data"));
    }


    // 2. getAllBooks

    @Test
    void getAllBooks_returnsList() {
        List<Book> books = List.of(
                new Book(1L, "Title1", "Author1", 1),
                new Book(2L, "Title2", "Author2", 2)
        );

        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        verify(bookRepository).findAll();
    }

    @Test
    void getAllBooks_emptyList() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<Book> result = bookService.getAllBooks();

        assertTrue(result.isEmpty());
    }

    // 3. getBookById

    @Test
    void getBookById_found_returnsBook() {
        Book book = new Book(1L, "Title", "Author", 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertEquals("Title", result.getTitle());
    }

    @Test
    void getBookById_notFound_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(1L));
        assertTrue(ex.getMessage().contains("Книгу з ID 1 не знайдено"));
    }

    // 4. updateBook

    @Test
    void updateBook_changeTitleAuthor_existingBook_mergesCopies() {
        Book current = new Book(1L, "OldTitle", "OldAuthor", 2);
        Book details = new Book(null, "NewTitle", "NewAuthor", 3);
        Book existing = new Book(2L, "NewTitle", "NewAuthor", 5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(current));
        when(bookRepository.findByTitleAndAuthor("NewTitle", "NewAuthor")).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        Book result = bookService.updateBook(1L, details);

        // existing copies 5 + current copies 2 = 7
        assertEquals(7, result.getCopiesAmount());
        verify(bookRepository).delete(current);
        verify(bookRepository).save(existing);
    }

    @Test
    void updateBook_changeTitleAuthor_noExisting_updatesCurrent() {
        Book current = new Book(1L, "OldTitle", "OldAuthor", 2);
        Book details = new Book(null, "NewTitle", "NewAuthor", 3);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(current));
        when(bookRepository.findByTitleAndAuthor("NewTitle", "NewAuthor")).thenReturn(Optional.empty());
        when(bookRepository.save(current)).thenAnswer(i -> i.getArgument(0));

        Book result = bookService.updateBook(1L, details);

        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(3, result.getCopiesAmount());
        verify(bookRepository).save(current);
    }

    @Test
    void updateBook_onlyCopiesAmountChanged_updatesCopies() {
        Book current = new Book(1L, "Title", "Author", 2);
        Book details = new Book(null, "Title", "Author", 5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(current));
        when(bookRepository.save(current)).thenAnswer(i -> i.getArgument(0));

        Book result = bookService.updateBook(1L, details);

        assertEquals(5, result.getCopiesAmount());
        verify(bookRepository).save(current);
    }

    @Test
    void updateBook_bookNotFound_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookService.updateBook(1L, new Book()));

        assertTrue(ex.getMessage().contains("Книгу з ID 1 не знайдено"));
    }

    // 5. deleteBook

    @Test
    void deleteBook_existingBook_deletesBook() {
        Book book = new Book(1L, "Title", "Author", 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_bookNotFound_throwsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookService.deleteBook(1L));

        assertTrue(ex.getMessage().contains("Книгу з ID 1 не знайдено"));
    }

    // 6. findByTitle

    @Test
    void findByTitle_found_returnsBook() {
        Book book = new Book(1L, "Title", "Author", 1);
        when(bookRepository.findByTitle("Title")).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findByTitle("Title");

        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
    }

    @Test
    void findByTitle_notFound_returnsEmpty() {
        when(bookRepository.findByTitle("Title")).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findByTitle("Title");

        assertTrue(result.isEmpty());
    }

    // 7. findByAuthor

    @Test
    void findByAuthor_found_returnsBooks() {
        List<Book> books = List.of(
                new Book(1L, "Title1", "Author", 1),
                new Book(2L, "Title2", "Author", 2)
        );

        when(bookRepository.findByAuthor("Author")).thenReturn(books);

        List<Book> result = bookService.findByAuthor("Author");

        assertEquals(2, result.size());
    }

    @Test
    void findByAuthor_notFound_returnsEmptyList() {
        when(bookRepository.findByAuthor("Author")).thenReturn(List.of());

        List<Book> result = bookService.findByAuthor("Author");

        assertTrue(result.isEmpty());
    }

    // 8. findByTitleAndAuthor

    @Test
    void findByTitleAndAuthor_found_returnsBook() {
        Book book = new Book(1L, "Title", "Author", 1);
        when(bookRepository.findByTitleAndAuthor("Title", "Author")).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findByTitleAndAuthor("Title", "Author");

        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
    }

    @Test
    void findByTitleAndAuthor_notFound_returnsEmpty() {
        when(bookRepository.findByTitleAndAuthor("Title", "Author")).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findByTitleAndAuthor("Title", "Author");

        assertTrue(result.isEmpty());
    }
}
