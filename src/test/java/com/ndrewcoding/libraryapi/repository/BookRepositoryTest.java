package com.ndrewcoding.libraryapi.repository;

import com.ndrewcoding.libraryapi.model.entity.Book;
import com.ndrewcoding.libraryapi.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class BookRepositoryTest {
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Must return true when a Book with the given ISBN exists in Database")
    public void returnTrueWhenIsbnExists() {
        String isbn = "123";

        Book book = Book.builder().title("My Title").author("My Author").isbn(isbn).build();

        testEntityManager.persist(book);

        boolean isbnExists = bookRepository.existsByIsbn(isbn);

        assertThat(isbnExists).isTrue();
    }

    @Test
    @DisplayName("Mus return false when a Book with the given ISBN does not exist in Database")
    public void returnFalseWhenIsbnDoesNotExist() {
        String isbn = "123";

        boolean isbnExists = bookRepository.existsByIsbn(isbn);

        assertThat(isbnExists).isFalse();
    }
}
