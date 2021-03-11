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
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
    @DisplayName("Must throw a Business Exception when trying to registrate a Book with existing ISBN")
    public void saveBookThrowsBusinessExceptionOnExistingIsbn() {
        Book book = createValidBook();

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN already registered!");

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    private Book createValidBook() {
        return Book.builder().title("My Book").author("My Author").isbn("001").build();
    }
}
