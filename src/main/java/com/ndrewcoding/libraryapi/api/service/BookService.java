package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface BookService {
    Book save(Book book);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(Book bookFilter, Pageable pageRequest);

    Optional<Book> getByIsbn(String isbn);
}
