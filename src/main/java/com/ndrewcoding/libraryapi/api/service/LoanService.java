package com.ndrewcoding.libraryapi.api.service;

import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import org.springframework.stereotype.Service;

@Service
public interface LoanService {

    Loan save(Loan loan);
}
