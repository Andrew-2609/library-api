package com.ndrewcoding.libraryapi.api.dto;

import lombok.Data;

@Data
public class LoanFilterDTO {
    private Long id;
    private String isbn;
    private String customer;
    private BookDTO bookDTO;
}
