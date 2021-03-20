package com.ndrewcoding.libraryapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndrewcoding.libraryapi.api.dto.LoanDTO;
import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.service.BookService;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Must make a Loan")
    public void createLoanTest() throws Exception {
        LoanDTO loanDTO = createNewLoanDTO();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book foundedBook = Book.builder().isbn("123").build();

        BDDMockito.given(bookService.getByIsbn("123")).willReturn(Optional.of(foundedBook));

        Loan loan = Loan.builder().id(1L).customer("Andrew").book(foundedBook).loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Must return an error when trying to create a Loan of a nonexistent Book")
    public void invalidIsbnCreateLoanTest() throws Exception {
        LoanDTO loanDTO = createNewLoanDTO();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.getByIsbn(Mockito.anyString())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is no Book with this ISBN."));
    }

    @Test
    @DisplayName("Must return an error when trying to create a Loan of an already loaned Book")
    public void bookAlreadyLoanedWhenCreatingNewLoanTest() throws Exception {
        LoanDTO loanDTO = createNewLoanDTO();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        Book foundedBook = Book.builder().id(1L).isbn("123").build();

        BDDMockito.given(bookService.getByIsbn("123")).willReturn(Optional.of(foundedBook));

        BDDMockito
                .given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    private LoanDTO createNewLoanDTO() {
        return LoanDTO.builder().isbn("123").customer("Andrew").build();
    }
}
