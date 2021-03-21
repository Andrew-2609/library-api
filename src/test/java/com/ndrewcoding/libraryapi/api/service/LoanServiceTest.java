package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.model.repository.LoanRepository;
import com.ndrewcoding.libraryapi.api.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;

    @MockBean
    LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Must save a Loan")
    public void saveLoanTest() {
        Book book = Book.builder().id(1L).build();

        Loan givenLoan = createValidLoan(book);

        Loan savedLoan = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .customer("Andrew")
                .book(book)
                .build();

        Mockito
                .when(loanRepository.existsByBookAndHasNotBeenReturned(book))
                .thenReturn(false);

        Mockito.when(loanRepository.save(givenLoan)).thenReturn(savedLoan);

        Loan finalLoan = loanService.save(givenLoan);

        assertThat(finalLoan.getId()).isEqualTo(savedLoan.getId());
        assertThat(finalLoan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(finalLoan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(finalLoan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Must throw a Business Error when saving a Loan with an already loaned Book")
    public void saveLoanedBookTest() {
        Book book = Book.builder().id(1L).build();

        Loan givenLoan = createValidLoan(book);

        Mockito
                .when(loanRepository.existsByBookAndHasNotBeenReturned(book))
                .thenReturn(true);

        Throwable exception = catchThrowable(() -> loanService.save(givenLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(loanRepository, never()).save(givenLoan);
    }

    @Test
    @DisplayName("Must obtain the information of a Loan with the given ID")
    public void getLoanDetailsTest() {
        long id = 1L;

        Book book = Book.builder().id(id).build();

        Loan loan = createValidLoan(book);

        loan.setId(id);

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> foundedLoan = loanService.getById(id);

        assertThat(foundedLoan).isPresent();
        assertThat(foundedLoan.get().getId()).isEqualTo(loan.getId());
        assertThat(foundedLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(foundedLoan.get().getBook()).isEqualTo(loan.getBook());
        assertThat(foundedLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(loanRepository).findById(id);
    }

    private Loan createValidLoan(Book book) {
        return Loan.builder().book(book).customer("Andrew").loanDate(LocalDate.now()).build();
    }
}
