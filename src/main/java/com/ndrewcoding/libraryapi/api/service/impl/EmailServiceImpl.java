package com.ndrewcoding.libraryapi.api.service.impl;

import com.ndrewcoding.libraryapi.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.default-sender}")
    private String sender;

    @Override
    public void sendMails(String message, List<String> mailsList) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        String[] mails = mailsList.toArray(new String[0]);

        mailMessage.setFrom(sender);
        mailMessage.setSubject("Overdue book loan");
        mailMessage.setText(message);
        mailMessage.setTo(mails);

        javaMailSender.send(mailMessage);
    }
}
