package com.ndrewcoding.libraryapi.service;

import com.ndrewcoding.libraryapi.exception.BusinessException;
import com.ndrewcoding.libraryapi.model.entity.Book;
import com.ndrewcoding.libraryapi.model.repository.BookRepository;
import com.ndrewcoding.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Must save a Book")
    public void saveBookTest() {
        Book book = createValidBook();

        Mockito.when(bookRepository.save(book))
                .thenReturn(Book.builder()
                        .id(1L)
                        .title("My Book")
                        .author("My Author")
                        .isbn("001")
                        .build());

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Book savedBook = bookService.save(book);

        assertThat(savedBook.getId()).isNotNull().isEqualTo(1L);
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Must throw a Business Exception when trying to register a Book with existing ISBN")
    public void saveBookThrowsBusinessExceptionOnExistingIsbn() {
        Book book = createValidBook();

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN already registered!");

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Must return a Book with the given id")
    public void getByIdTest() {
        long id = 1L;

        Book book = createValidBook();

        book.setId(id);

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundedBook = bookService.getById(id);

        assertThat(foundedBook.isPresent()).isTrue();
        assertThat(foundedBook.get().getId()).isEqualTo(id);
        assertThat(foundedBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundedBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundedBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Must return empty when given id doesn't exist")
    public void bookNotFoundByIdTest() {
        long id = 1L;

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> searchedBook = bookService.getById(id);

        assertThat(searchedBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Must delete the Book with the given ID")
    public void deleteBookTest() {
        Book book = Book.builder().id(1L).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Delete must throw IllegalArgumentException when given Book is not valid")
    public void deleteInvalidBookTest() {
        Book book = Book.builder().build();

        Throwable exception = Assertions.catchThrowable(() -> bookService.delete(book));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is no Book with this ID.");

        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Must successfully update a Book with the given ID")
    public void updateBookTest() {
        long id = 1L;

        Book originalBook = Book.builder().id(id).build();

        Book updatingBook = createValidBook();

        updatingBook.setId(id);

        Mockito.when(bookRepository.save(originalBook)).thenReturn(updatingBook);

        Book finalBook = bookService.update(originalBook);

        assertThat(finalBook.getId()).isEqualTo(updatingBook.getId());
        assertThat(finalBook.getTitle()).isEqualTo(updatingBook.getTitle());
        assertThat(finalBook.getAuthor()).isEqualTo(updatingBook.getAuthor());
        assertThat(finalBook.getIsbn()).isEqualTo(updatingBook.getIsbn());
    }

    @Test
    @DisplayName("Must throw IllegalArgumentException when updating a Book with invalid ID")
    public void updateInvalidBookTest() {
        Book book = Book.builder().build();

        Throwable exception = Assertions.catchThrowable(() -> bookService.update(book));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is no Book with this ID.");

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    private Book createValidBook() {
        return Book.builder().title("My Book").author("My Author").isbn("001").build();
    }
}
