package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.exception.IdempotencyConflictException;
import com.yowyob.template.domain.exception.MissingIdempotencyKeyException;
import com.yowyob.template.domain.exception.StockFullException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestion centralisée des exceptions métier vers le format
 * {@link ProblemDetail} (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Nom : {@code handleIdempotencyConflict}
     * <p>
     * Description : conflit lorsque la même {@code Idempotency-Key} est rejouée
     * avec un corps différent.
     * </p>
     *
     * @param ex exception métier
     * @return problème HTTP 409
     */
    @ExceptionHandler(IdempotencyConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ProblemDetail> handleIdempotencyConflict(IdempotencyConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflit d’idempotence");
        problem.setType(URI.create("errors/idempotency-conflict"));
        return Mono.just(problem);
    }

    /**
     * Nom : {@code handleMissingIdempotencyKey}
     * <p>
     * Description : en-tête obligatoire absent sur une route idempotente.
     * </p>
     *
     * @param ex exception métier
     * @return problème HTTP 400
     */
    @ExceptionHandler(MissingIdempotencyKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleMissingIdempotencyKey(MissingIdempotencyKeyException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Idempotency-Key manquante");
        problem.setType(URI.create("errors/missing-idempotency-key"));
        return Mono.just(problem);
    }

    /**
     * Nom : {@code handleStockException}
     * <p>
     * Description : convertit un dépassement de stock en réponse HTTP 409.
     * </p>
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

    /**
     * Nom : {@code handleWebExchangeBindException}
     * <p>
     * Description : agrège les erreurs de validation Bean Validation sur les corps
     * JSON WebFlux.
     * </p>
     *
     * @param ex liaison invalide renvoyée par le binder réactif
     * @return problème HTTP 400 avec propriétés par champ
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête invalide");
        problem.setTitle("Erreur de validation");
        problem.setType(URI.create("errors/validation"));
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalide");
        }
        problem.setProperty("fieldErrors", fieldErrors);
        return Mono.just(problem);
    }

    /**
     * Nom : {@code handleIllegalArgumentException}
     * <p>
     * Description : réponses 400 pour arguments métier invalides (ex. plafond de
     * montant).
     * </p>
     *
     * @param ex message utilisateur
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

    /**
     * Nom : {@code handleConstraintViolationException}
     * <p>
     * Description : paramètres de requête ou arguments de méthode invalidés par
     * Bean
     * Validation (ex. pagination {@code GET /wallets}).
     * </p>
     *
     * @param ex violations détectées
     * @return problème HTTP 400
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Paramètres invalides");
        problem.setTitle("Erreur de validation");
        problem.setType(URI.create("errors/constraint-violation"));
        String detail = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        problem.setDetail(detail.isEmpty() ? "Paramètres invalides" : detail);
        return Mono.just(problem);
    }

    /**
     * Nom : {@code handleServerWebInputException}
     * <p>
     * Description : entrée WebFlux invalide (paramètres manquants ou mal formés).
     * </p>
     *
     * @param ex erreur de liaison d’entrée
     * @return problème HTTP 400
     */
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ProblemDetail> handleServerWebInputException(ServerWebInputException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getReason() != null ? ex.getReason() : "Entrée invalide");
        problem.setTitle("Requête invalide");
        problem.setType(URI.create("errors/bad-input"));
        return Mono.just(problem);
    }
}
