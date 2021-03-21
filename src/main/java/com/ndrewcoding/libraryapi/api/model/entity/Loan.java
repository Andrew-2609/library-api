package com.ndrewcoding.libraryapi.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loan {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String customer;

    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;

    @Column
    private LocalDate loanDate;

    @Column
    private boolean returned;
}
