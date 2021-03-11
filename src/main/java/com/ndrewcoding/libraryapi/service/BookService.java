package com.ndrewcoding.libraryapi.service;

import com.ndrewcoding.libraryapi.model.entity.Book;
import org.springframework.stereotype.Service;

@Service
public interface BookService {
    Book save(Book book);
}
