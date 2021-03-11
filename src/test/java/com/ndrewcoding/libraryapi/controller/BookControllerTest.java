package com.ndrewcoding.libraryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndrewcoding.libraryapi.dto.BookDTO;
import com.ndrewcoding.libraryapi.exception.BusinessException;
import com.ndrewcoding.libraryapi.model.entity.Book;
import com.ndrewcoding.libraryapi.service.BookService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Must successfully create a Book")
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createNewBookDTO();

        Book savedBook = Book.builder().id(1L).title("Title").author("Author").isbn("001").build();

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Must throw a Validation Error when creating a Book with insufficient data")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath(
                        "errors",
                        hasSize(Book.class.getDeclaredFields().length - 1
                        )));
    }

    @Test
    @DisplayName("Must throw an Error when creating a Book with an ISBN that other Book already has")
    public void createBookWithDuplicatedIsbn() throws Exception {

        BookDTO bookDTO = createNewBookDTO();

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        String existingIsbnErrorMessage = "ISBN already registered!";

        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(existingIsbnErrorMessage));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(existingIsbnErrorMessage));
    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().title("Title").author("Author").isbn("001").build();
    }
}
