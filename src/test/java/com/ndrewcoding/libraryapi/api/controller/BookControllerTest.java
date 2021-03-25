package com.ndrewcoding.libraryapi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndrewcoding.libraryapi.api.dto.BookDTO;
import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Book;
import com.ndrewcoding.libraryapi.api.service.BookService;
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

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Must return the details of a Book")
    public void getBookTest() throws Exception {
        BookDTO bookDTO = createNewBookDTO();

        long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(bookDTO.getTitle())
                .author(bookDTO.getAuthor())
                .isbn(bookDTO.getIsbn())
                .build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Must return Resource Not Found when the searched Book does not exist.")
    public void bookNotFoundTest() throws Exception {

        long id = 1L;

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

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
                .andExpect(status().isCreated())
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "errors",
                        hasSize(Book.class.getDeclaredFields().length - 1
                        )));
    }

    @Test
    @DisplayName("Must throw an Error when creating a Book with an ISBN that other Book already has")
    public void createBookWithDuplicatedIsbnTest() throws Exception {

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

    @Test
    @DisplayName("Must update an existent Book")
    public void updateBookTest() throws Exception {
        long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        Book originalBook = Book.builder().id(id).title("Original Title").author("Original Author").isbn("336").build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(originalBook));

        Book updatedBook = Book.builder().id(originalBook.getId()).title("Updated Title").author("Updated Author").isbn(originalBook.getIsbn()).build();

        BDDMockito.given(bookService.update(originalBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("title").value(updatedBook.getTitle()))
                .andExpect(jsonPath("author").value(updatedBook.getAuthor()))
                .andExpect(jsonPath("isbn").value(originalBook.getIsbn()));
    }

    @Test
    @DisplayName("Must throw 404 Status when trying to update an nonexistent Book")
    public void updateNonexistentBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must delete a Book based on its Id")
    public void deleteBookTest() throws Exception {

        long id = 1L;

        Book book = Book.builder().id(id).build();

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id));

        mvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Must throw Resource Not Found when trying to delete an nonexistent Book")
    public void deleteNonexistentBookTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1L));

        mvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Must filter Books")
    public void findBooksTest() throws Exception {
        Book book = Book.builder()
                .id(1L)
                .title(createNewBookDTO().getTitle())
                .author(createNewBookDTO().getAuthor())
                .isbn(createNewBookDTO().getIsbn())
                .build();

        BDDMockito
                .given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(book), PageRequest.of(0, 100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("pageable.pageNumber").value(0))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("totalElements").value(1));
    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().title("Title").author("Author").isbn("001").build();
    }
}
