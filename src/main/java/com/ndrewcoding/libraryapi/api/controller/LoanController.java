package com.ndrewcoding.libraryapi.api.controller;

import com.ndrewcoding.libraryapi.api.dto.BookDTO;
import com.ndrewcoding.libraryapi.api.dto.LoanDTO;
import com.ndrewcoding.libraryapi.api.dto.LoanFilterDTO;
import com.ndrewcoding.libraryapi.api.dto.ReturnedLoanDTO;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.service.BookService;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Api("Loan API")
@Slf4j
public class LoanController {
    private final LoanService loanService;
    private final BookService bookService;

    private final ModelMapper modelMapper;

    @GetMapping
    @ApiOperation("Finds all existing Loans")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Loans successfully returned")
    })
    public Page<LoanDTO> find(LoanFilterDTO filter, Pageable pageable) {
        Page<Loan> loans = loanService.find(filter, pageable);

        List<LoanDTO> loansList = pageLoanToListLoanDTO(loans, modelMapper);

        return new PageImpl<>(loansList, pageable, loans.getTotalElements());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a Loan based on its DTO details")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Loan successfully created"),
            @ApiResponse(code = 400, message = "No Book with the given ISBN")
    })
    public Long create(@RequestBody LoanDTO loanDTO) {
        Book foundedBook = bookService.getByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "There is no Book with this ISBN."
                        )
                );

        Loan entity = Loan.builder()
                .book(foundedBook)
                .customer(loanDTO.getCustomer())
                .customerEmail(loanDTO.getCustomerEmail())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);

        return entity.getId();
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Sets a Loan as 'returned'")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Book successfully returned"),
            @ApiResponse(code = 404, message = "No Loan for the given Book")
    })
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO returnedLoanDTO) {
        Loan foundedLoan = loanService
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no Loan for this Book"));

        foundedLoan.setReturned(returnedLoanDTO.isReturned());

        loanService.update(foundedLoan);
        log.info("loan of id {} returned", foundedLoan.getId());
    }

    protected static List<LoanDTO> pageLoanToListLoanDTO(Page<Loan> loans, ModelMapper modelMapper) {
        return loans
                .getContent()
                .stream()
                .map(loan -> {
                    Book book = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBookDTO(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
    }
}
