package com.ndrewcoding.libraryapi.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanFilterDTO {
    private String isbn;
    private String customer;
}
