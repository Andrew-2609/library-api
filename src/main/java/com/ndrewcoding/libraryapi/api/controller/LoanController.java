package com.ndrewcoding.libraryapi.api.controller;

import com.ndrewcoding.libraryapi.api.dto.LoanDTO;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.service.BookService;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO loanDTO) {
        Book foundedBook = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "There is no Book with this ISBN."
                        )
                );

        Loan entity = Loan.builder()
                .book(foundedBook)
                .customer(loanDTO.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);

        return entity.getId();
    }


}
