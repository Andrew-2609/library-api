package com.ndrewcoding.libraryapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndrewcoding.libraryapi.api.dto.BookDTO;
import com.ndrewcoding.libraryapi.api.dto.LoanDTO;
import com.ndrewcoding.libraryapi.api.dto.LoanFilterDTO;
import com.ndrewcoding.libraryapi.api.dto.ReturnedLoanDTO;
import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.service.BookService;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import com.ndrewcoding.libraryapi.api.service.LoanServiceTest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
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

        Loan loan = Loan.builder().id(1L).customer("Andrew").customerEmail("andrew@email.com")
                .book(foundedBook).loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = postLoanRequestBuilder(json);

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

        MockHttpServletRequestBuilder request = postLoanRequestBuilder(json);

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

        MockHttpServletRequestBuilder request = postLoanRequestBuilder(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Must return a Book to the Library")
    public void returnBookTest() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        long id = 1L;

        Loan loan = Loan.builder().id(id).build();

        BDDMockito.given(loanService.getById(id)).willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = patchLoanByIdRequestBuilder(id, json);

        mvc.perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Must return 404 status code when returning a nonexistent Book to the Library")
    public void returnNonexistentBook() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = patchLoanByIdRequestBuilder(1L, json);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is no Loan for this Book"));
    }

    @Test
    @DisplayName("Must filter Loans")
    public void findLoansTest() throws Exception {
        long genericId = 1L;

        Book book = Book.builder().id(genericId).isbn("123").build();

        Loan loan = LoanServiceTest.createValidLoan(book);

        loan.setId(genericId);

        BDDMockito
                .given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                loan.getBook().getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("pageable.pageNumber").value(0))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("totalElements").value(1));
    }

    private LoanDTO createNewLoanDTO() {
        BookDTO bookDTO = BookControllerTest.createNewBookDTO();
        return LoanDTO.builder().id(1L).isbn("123").customer("Andrew").customerEmail("andrew@email.com").bookDTO(bookDTO).build();
    }

    private MockHttpServletRequestBuilder postLoanRequestBuilder(String json) {
        return MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private MockHttpServletRequestBuilder patchLoanByIdRequestBuilder(long id, String json) {
        return MockMvcRequestBuilders
                .patch(LOAN_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
    }
}
