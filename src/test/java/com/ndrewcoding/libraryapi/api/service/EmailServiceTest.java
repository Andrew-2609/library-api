package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class EmailServiceTest {

    EmailService emailService;
    SimpleMailMessage mailMessage;

    @MockBean
    JavaMailSender javaMailSender;

    @BeforeEach
    public void setUp() {
        this.emailService = new EmailServiceImpl(javaMailSender);
    }

    @Test
    @DisplayName("must successfully send a mail")
    public void sendMailTest() {
        mailMessage = new SimpleMailMessage();

        List<String> mailsList = new ArrayList<>();

        mailsList.add("email1@gmail.com");
        mailsList.add("email2@gmail.com");

        String[] mails = mailsList.toArray(new String[0]);

        String from = "andrew@email.com";
        String subject = "Overdue book loan";
        String text = "You have (an) overdue book loan(a)s!";

        mailMessage.setFrom(from);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailMessage.setTo(mails);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> emailService.sendMails("", mailsList));

        javaMailSender.send(mailMessage);

        Mockito.verify(javaMailSender, Mockito.times(1)).send(mailMessage);

        assertThat(mailMessage.getFrom()).isEqualTo(from);
        assertThat(mailMessage.getSubject()).isEqualTo(subject);
        assertThat(mailMessage.getText()).isEqualTo(text);
        assertThat(mailMessage.getTo()).isEqualTo(mails);
    }
}
