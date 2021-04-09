package com.ndrewcoding.libraryapi.api.service;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ScheduleServiceTest {

    LoanService loanService;
    ScheduleService scheduleService;

    @MockBean
    EmailService emailService;

    @MockBean
    LoanRepository loanRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @BeforeEach
    public void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
        this.scheduleService = new ScheduleService(loanService, emailService);
    }

    @Test
    @DisplayName("Must successfully send a mail")
    public void sendMailToCustomersWithOverdueLoansTest() {
        Book book = Book.builder().id(1L).build();

        Loan loan = LoanServiceTest.createValidLoan(book);

        List<Loan> overdueLoansList = Collections.singletonList(loan);

        Mockito
                .when(loanRepository.findByLoanDateLessThanAndNotReturned(Mockito.any(LocalDate.class)))
                .thenReturn(overdueLoansList);

        scheduleService.sendMailToCustomersWithOverdueLoans();

        Mockito.verify(loanRepository, Mockito.times(1))
                .findByLoanDateLessThanAndNotReturned(Mockito.any(LocalDate.class));

        Mockito.verify(emailService, Mockito.times(1))
                .sendMails(null, Collections.singletonList(overdueLoansList.get(0).getCustomerEmail()));
    }
}
