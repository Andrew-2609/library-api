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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        Loan persistedLoan = createAndPersistALoanAndItsBook();

        boolean exists = loanRepository.existsByBookAndHasNotBeenReturned(persistedLoan.getBook());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Must find a Loan by its Book's ISBN or by its Customer")
    public void findByBookIsbnOrCustomerTest() {
        Loan persistedLoan = createAndPersistALoanAndItsBook();

        Page<Loan> loansResult = loanRepository.findByBookIsbnOrCustomer(
                "123", "Andrew", PageRequest.of(0, 10)
        );

        assertThat(loansResult.getContent()).contains(persistedLoan);

        assertThat(loansResult.getContent()).hasSize(1);

        assertThat(loansResult.getTotalElements()).isEqualTo(1);

        assertThat(loansResult.getPageable().getPageNumber()).isEqualTo(0);

        assertThat(loansResult.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Must get Loans that were loaned at most three days ago and that are not returned")
    public void findByLoanDateLessThanAndNotReturned() {
        Loan loan = createAndPersistALoanAndItsBook();

        loan.setLoanDate(LocalDate.now().minus(5, ChronoUnit.DAYS));

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minus(4, ChronoUnit.DAYS));

        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Must return empty when there are no overdue Loans")
    public void findByLoanDateLessThanAndNotReturnedReturnsEmptyWhenLoansAreUpToDate() {
        createAndPersistALoanAndItsBook();

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minus(4, ChronoUnit.DAYS));

        assertThat(result).isEmpty();
    }

    private Loan createNewLoan(Book book) {
        return Loan.builder().book(book).customer("Andrew").loanDate(LocalDate.now()).build();
    }

    public Loan createAndPersistALoanAndItsBook() {
        Book book = BookRepositoryTest.createNewBook("123");

        testEntityManager.persist(book);

        Loan loan = createNewLoan(book);

        testEntityManager.persist(loan);

        return loan;
    }
}
