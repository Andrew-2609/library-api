package com.ndrewcoding.libraryapi.api.controller;

import com.ndrewcoding.libraryapi.api.dto.BookDTO;
import com.ndrewcoding.libraryapi.api.dto.LoanDTO;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.service.BookService;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

    public BookController(BookService bookService, ModelMapper modelMapper, ObjectProvider<LoanService> loanService) {
        this.bookService = bookService;
        this.modelMapper = modelMapper;
        this.loanService = loanService.getIfAvailable();
    }

    @GetMapping("{id}")
    @ApiOperation("Gets the details of a Book by its ID")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Book successfully found"),
            @ApiResponse(code = 404, message = "Book not found")
    })
    public BookDTO get(@PathVariable Long id) {
        return bookService
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Finds all existing Books")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Books successfully returned")
    })
    public Page<BookDTO> find(BookDTO bookDTO, Pageable pageRequest) {
        Book filter = modelMapper.map(bookDTO, Book.class);

        Page<Book> result = bookService.find(filter, pageRequest);

        List<BookDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a Book based on its DTO details")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Book successfully created"),
            @ApiResponse(code = 400, message = "ISBN already registered")
    })
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO) {
        Book entity = modelMapper.map(bookDTO, Book.class);

        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Updates the Book with the given ID using its given DTO details (doesnt update the ISBN")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Book successfully updated"),
            @ApiResponse(code = 404, message = "Book not found")
    })
    public BookDTO update(@PathVariable Long id, BookDTO bookDTO) {
        log.info("updating the Book of id: {}", id);
        return bookService
                .getById(id)
                .map(book -> {
                    book.setTitle(bookDTO.getTitle());
                    book.setAuthor(bookDTO.getAuthor());

                    book = bookService.update(book);

                    return modelMapper.map(book, BookDTO.class);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes the Book with the given ID")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book successfully deleted"),
            @ApiResponse(code = 400, message = "Nonexistent Book")
    })
    public void delete(@PathVariable Long id) {
        log.info("deleting the Book of id: {}", id);
        Book book = bookService
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        bookService.delete(book);
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Gets all the Loans of the Book with the given ID")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Book Loans successfully returned"),
            @ApiResponse(code = 404, message = "Book not found")
    })
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book foundedBook = bookService
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> loansResult = loanService.getLoansByBook(foundedBook, pageable);

        List<LoanDTO> listLoans = LoanController.pageLoanToListLoanDTO(loansResult, modelMapper);

        return new PageImpl<>(listLoans, pageable, loansResult.getTotalElements());
    }
}
