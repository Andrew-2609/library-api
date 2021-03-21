package com.ndrewcoding.libraryapi.repository;

import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    LoanRepository loanRepository;

    @Test
    @DisplayName("Must verify if there is a Loan of a not yet returned given Book")
    public void existsByBookAndHasNotBeenReturnedTest() {
        Book book = BookRepositoryTest.createNewBook("123");

        Loan loan = createNewLoan(book);

        testEntityManager.persist(book);

        testEntityManager.persist(loan);

        boolean exists = loanRepository.existsByBookAndHasNotBeenReturned(book);

        assertThat(exists).isTrue();
    }

    private Loan createNewLoan(Book book) {
        return Loan.builder().book(book).customer("Andrew").loanDate(LocalDate.now()).build();
    }
}
