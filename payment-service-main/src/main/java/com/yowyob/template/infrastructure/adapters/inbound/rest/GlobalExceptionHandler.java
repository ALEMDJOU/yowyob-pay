package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.exception.StockFullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Gestion centralisée des exceptions métier vers le format {@link ProblemDetail} (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Convertit un dépassement de stock en réponse HTTP 409.
     *
     * @param ex exception fonctionnelle
     * @return détail de problème avec type {@code errors/stock-full}
     */
    @ExceptionHandler(StockFullException.class)
    public ProblemDetail handleStockException(StockFullException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Stock Overflow");
        problem.setType(URI.create("errors/stock-full"));
        return problem;
    }
}