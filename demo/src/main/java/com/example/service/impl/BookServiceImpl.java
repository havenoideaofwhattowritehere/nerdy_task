package com.example.service.impl;

import com.example.entity.Book;
import com.example.exception.ValidationException;
import com.example.repository.BookRepository;
import com.example.service.BookService;
import com.example.validator.BookValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookValidator bookValidator;

    @Override
    public Book createBook(Book book) {
        DataBinder binder = new DataBinder(book);
        binder.setValidator(bookValidator);
        binder.validate();

        if (binder.getBindingResult().hasErrors()) {
            throw new ValidationException(binder.getBindingResult().getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", ")));
        }

        String title = book.getTitle().trim();
        String author = book.getAuthor().trim();

        return bookRepository.findByTitleAndAuthor(title, author)
                .map(existingBook -> {
                    existingBook.setCopiesAmount(existingBook.getCopiesAmount() + 1);
                    return bookRepository.save(existingBook);
                })
                .orElseGet(() -> {
                    book.setTitle(title);
                    book.setAuthor(author);
                    book.setCopiesAmount(1);
                    return bookRepository.save(book);
                });
    }


    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Книгу з ID " + id + " не знайдено"));
    }

    @Override
    @Transactional
    public Book updateBook(Long id, Book bookDetails) {
        Book currentBook = getBookById(id);
        String title = bookDetails.getTitle().trim();
        String author = bookDetails.getAuthor().trim();

        // Якщо змінилися і назва, і автор
        if (!currentBook.getTitle().equalsIgnoreCase(title) ||
                !currentBook.getAuthor().equalsIgnoreCase(author)) {

            Optional<Book> existingBook = bookRepository.findByTitleAndAuthor(title, author);

            if (existingBook.isPresent()) {
                // Якщо така книга вже існує, збільшуємо її кількість
                Book bookToUpdate = existingBook.get();
                bookToUpdate.setCopiesAmount(bookToUpdate.getCopiesAmount() + currentBook.getCopiesAmount());
                bookRepository.save(bookToUpdate);

                // Видаляємо стару книгу
                bookRepository.delete(currentBook);

                return bookToUpdate;
            } else {
                // Якщо такої книги не існує, оновлюємо поточну
                currentBook.setTitle(title);
                currentBook.setAuthor(author);
                currentBook.setCopiesAmount(bookDetails.getCopiesAmount());
                return bookRepository.save(currentBook);
            }
        }

        // Якщо змінюється тільки кількість копій
        currentBook.setCopiesAmount(bookDetails.getCopiesAmount());
        return bookRepository.save(currentBook);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    @Override
    public Optional<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title.trim());
    }

    @Override
    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthor(author.trim());
    }

    @Override
    public Optional<Book> findByTitleAndAuthor(String title, String author) {
        return bookRepository.findByTitleAndAuthor(title.trim(), author.trim());
    }

}
