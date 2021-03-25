package com.ndrewcoding.libraryapi.api.service.impl;

import com.ndrewcoding.libraryapi.api.dto.LoanFilterDTO;
import com.ndrewcoding.libraryapi.api.exception.BusinessException;
import com.ndrewcoding.libraryapi.api.model.entity.Loan;
import com.ndrewcoding.libraryapi.api.model.repository.LoanRepository;
import com.ndrewcoding.libraryapi.api.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if (loanRepository.existsByBookAndHasNotBeenReturned(loan.getBook())) {
            throw new BusinessException("Book already loaned");
        }
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan foundedLoan) {
        return loanRepository.save(foundedLoan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
        return loanRepository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
    }
}
