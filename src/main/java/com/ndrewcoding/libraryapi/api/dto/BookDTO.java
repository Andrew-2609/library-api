package com.ndrewcoding.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private Long id;
    @NotEmpty(message = "The field 'title' cannot be null!")
    private String title;
    @NotEmpty(message = "The field 'author' cannot be null!")
    private String author;
    @NotEmpty(message = "The field 'isbn' cannot be null!")
    private String isbn;
}
