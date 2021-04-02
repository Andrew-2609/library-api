package com.ndrewcoding.libraryapi;

import com.ndrewcoding.libraryapi.api.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

    private final EmailService emailService;

    public LibraryApiApplication(EmailService emailService) {
        this.emailService = emailService;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    //Test purposes
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            List<String> emails = Collections.singletonList("library-api-7ba0b2@inbox.mailtrap.io");
            emailService.sendMails("Testing mail service", emails);
            System.out.println("Emails successfully sent now");
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(LibraryApiApplication.class, args);
    }

}
