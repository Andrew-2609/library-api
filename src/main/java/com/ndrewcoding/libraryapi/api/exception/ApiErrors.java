package com.ndrewcoding.libraryapi.api.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiErrors {
    @Getter
    private final List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException businessException) {
        this.errors = Collections.singletonList(businessException.getMessage());
    }

    public ApiErrors(ResponseStatusException responseStatusException) {
        this.errors = Collections.singletonList(responseStatusException.getReason());
    }
}
