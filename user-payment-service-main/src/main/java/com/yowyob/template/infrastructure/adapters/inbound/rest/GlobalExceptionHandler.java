package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.exception.StockFullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Traduction des exceptions métier vers des réponses HTTP structurées.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @param ex détail fonctionnel
     * @return problème HTTP 409 typé {@code errors/stock-full}
     */
    @ExceptionHandler(StockFullException.class)
    public ProblemDetail handleStockException(StockFullException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Stock Overflow");
        problem.setType(URI.create("errors/stock-full"));
        return problem;
    }
}
