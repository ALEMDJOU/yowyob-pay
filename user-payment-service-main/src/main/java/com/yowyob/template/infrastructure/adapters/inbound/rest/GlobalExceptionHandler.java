package com.yowyob.template.infrastructure.adapters.inbound.rest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.yowyob.template.domain.exception.StockFullException;

import reactor.core.publisher.Mono;

/**
 * Traduction des exceptions métier et de validation vers des réponses HTTP
 * structurées.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Nom : {@code handleStockException}
     * <p>
     * Description : convertit un dépassement de stock en réponse HTTP 409.
     * </p>
     *
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

    /**
     * Nom : {@code handleWebExchangeBindException}
     * <p>
     * Description : erreurs Bean Validation sur les corps JSON WebFlux.
     * </p>
     *
     * @param ex exception de liaison
     * @return problème HTTP 400
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalide");
        }
        String detail = fieldErrors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Requête invalide");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Erreur de validation");
        problem.setType(URI.create("errors/validation"));
        problem.setProperty("fieldErrors", fieldErrors);
        return Mono.just(problem);
    }

    /**
     * Erreurs métier auth (identifiants, email déjà pris, etc.).
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Erreur métier";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = "BUSINESS_ERROR";
        if ("Email déjà utilisé".equals(message)) {
            status = HttpStatus.CONFLICT;
            code = "EMAIL_ALREADY_USED";
        } else if ("Identifiants incorrects".equals(message) || "Agent introuvable".equals(message)) {
            status = HttpStatus.UNAUTHORIZED;
            code = "INVALID_CREDENTIALS";
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setTitle(status == HttpStatus.UNAUTHORIZED ? "Authentification échouée" : "Erreur métier");
        problem.setType(URI.create("errors/business"));
        problem.setProperty("code", code);
        return Mono.just(problem);
    }

    /**
     * Nom : {@code handleIllegalArgumentException}
     * <p>
     * Description : arguments métier invalides (ex. plafond de recharge).
     * </p>
     *
     * @param ex message d’erreur
     * @return problème HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Requête invalide");
        problem.setType(URI.create("errors/illegal-argument"));
        return Mono.just(problem);
    }
}
