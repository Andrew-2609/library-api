package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private static final String CRON_OVERDUE_LOANS = "0 0 0 1/1 * ?";

    @Value("${application.mail.overdueLoans.message}")
    private String message;

    private final LoanService loanService;
    private final EmailService emailService;

    @Scheduled(cron = CRON_OVERDUE_LOANS)
    void sendMailToCustomersWithOverdueLoans() {
        List<Loan> overdueLoans = loanService.getAllOverdueLoans();
        List<String> mailsList = overdueLoans
                .stream()
                .map(Loan::getCustomerEmail)
                .collect(Collectors.toList());

        emailService.sendMails(message, mailsList);
    }
}
