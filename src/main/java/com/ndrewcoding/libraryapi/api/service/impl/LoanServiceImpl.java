package com.ndrewcoding.libraryapi.api.service.impl;

import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.model.repository.LoanRepository;
import com.ndrewcoding.libraryapi.api.service.LoanService;

public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if(loanRepository.existsByBookAndHasNotBeenReturned(loan.getBook())) {
            throw new BusinessException("Book already loaned");
        }
        return loanRepository.save(loan);
    }
}
