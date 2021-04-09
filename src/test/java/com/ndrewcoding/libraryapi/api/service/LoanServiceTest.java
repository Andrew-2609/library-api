package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.dto.LoanFilterDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("Must update a Loan")
    public void updateLoanTest() {
        Book book = Book.builder().id(1L).build();

        Loan originalLoan = createValidLoan(book);

        originalLoan.setId(1L);

        originalLoan.setReturned(true);

        Mockito.when(loanRepository.save(Mockito.any(Loan.class))).thenReturn(originalLoan);

        Loan updatedLoan = loanService.update(originalLoan);

        assertThat(updatedLoan.isReturned()).isTrue();

        verify(loanRepository).save(originalLoan);
    }

    @Test
    @DisplayName("Must filter Loans by its properties")
    public void findLoansTest() {
        //scenery
        long genericId = 1L;

        Book book = Book.builder().id(genericId).build();

        Loan loan = createValidLoan(book);

        loan.setId(genericId);

        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Andrew").isbn("123").build();

        int pageSize = 10;

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        List<Loan> loansList = Collections.singletonList(loan);

        Page<Loan> page = new PageImpl<>(loansList, pageRequest, 1);

        Mockito
                .when(loanRepository
                        .findByBookIsbnOrCustomer(
                                Mockito.anyString(),
                                Mockito.anyString(),
                                Mockito.any(PageRequest.class))
                )
                .thenReturn(page);

        //execution
        Page<Loan> loansResult = loanService.find(loanFilterDTO, pageRequest);

        //verifications
        assertThat(loansResult.getTotalElements()).isEqualTo(1);
        assertThat(loansResult.getContent()).isEqualTo(loansList);
        assertThat(loansResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(loansResult.getPageable().getPageSize()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("Must filter the Loans of a given Book")
    public void findLoansByBookTest() {
        Book book = BookServiceTest.createValidBook();

        Loan loan = createValidLoan(book);

        int pageSize = 10;

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        List<Loan> loansList = Collections.singletonList(loan);

        Page<Loan> page = new PageImpl<>(loansList, pageRequest, 1);

        Mockito
                .when(loanRepository.findByBook(Mockito.any(Book.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> foundLoans = loanService.getLoansByBook(book, pageRequest);

        assertThat(foundLoans.getTotalElements()).isEqualTo(1);
        assertThat(foundLoans.getContent()).isEqualTo(loansList);
        assertThat(foundLoans.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(foundLoans.getPageable().getPageSize()).isEqualTo(pageSize);


    }

    public static Loan createValidLoan(Book book) {
        return Loan.builder().book(book).customer("Andrew").loanDate(LocalDate.now()).build();
    }
}
