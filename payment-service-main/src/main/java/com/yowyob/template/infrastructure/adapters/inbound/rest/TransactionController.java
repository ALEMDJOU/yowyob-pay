package com.yowyob.template.infrastructure.adapters.inbound.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yowyob.template.domain.model.TransactionType;
import com.yowyob.template.domain.ports.in.TransactionUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.TransactionRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.TransactionResponse;
import com.yowyob.template.infrastructure.mappers.TransactionMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * API {@code /api/v1/transactions} : création de recharge/paiement et
 * consultations.
 */
@Tag(name = "Transaction Management", description = "API for transaction management")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionUseCase useCase;
    private final TransactionMapper mapper;

    /**
     * Crée une transaction de type {@link TransactionType#RECHARGE} uniquement.
     *
     * @param requestMono corps validé
     * @return transaction créée (201)
     * @throws IllegalArgumentException réactive si le type n’est pas RECHARGE
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new recharge transaction", description = "Creates a new transaction of type RECHARGE. This endpoint is reserved for agents.")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request, only RECHARGE transactions are allowed")
    @ApiResponse(responseCode = "401", description = "Unauthorized, invalid or expired token")
    @ApiResponse(responseCode = "403", description = "Forbidden, requires ROLE_AGENT")
    public Mono<TransactionResponse> create(@RequestBody @Valid Mono<TransactionRequest> requestMono) {
        return requestMono
                .flatMap(request -> {
                    if (request.type() != TransactionType.RECHARGE) {
                        return Mono.error(
                                new IllegalArgumentException("Cet endpoint est réservé aux recharges via Agent"));
                    }
                    return Mono.just(request);
                })
                .map(mapper::toDomain)
                .flatMap(useCase::createTransaction)
                .map(mapper::toResponse);
    }

    /**
     * Crée une transaction de type {@link TransactionType#PAYMENT}.
     *
     * @param requestMono corps validé
     * @return transaction créée (201)
     * @throws IllegalArgumentException réactive si le type n’est pas PAYMENT
     */
    @PostMapping("/payment")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new payment transaction", description = "Creates a new transaction of type PAYMENT.")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Unauthorized, invalid or expired token")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public Mono<TransactionResponse> createPaymentTransaction(
            @RequestBody @Valid Mono<TransactionRequest> requestMono) {
        return requestMono
                .flatMap(request -> {
                    if (request.type() != TransactionType.PAYMENT) {
                        return Mono.error(
                                new IllegalArgumentException("Cet endpoint est réservé aux recharges de type PAYMENT"));
                    }
                    return Mono.just(request);
                })
                .map(mapper::toDomain)
                .flatMap(useCase::createTransaction)
                .map(mapper::toResponse);
    }

    /**
     * Détail d’une transaction par identifiant.
     *
     * @param id clé UUID
     * @return transaction trouvée ou erreur métier 404
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves transaction details by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    public Mono<TransactionResponse> findById(
            @Parameter(description = "ID of the transaction to retrieve") @PathVariable("id") UUID id) {
        return useCase.getTransactionById(id)
                .map(mapper::toResponse);
    }

    /**
     * Historique des transactions pour un portefeuille donné.
     *
     * @param walletId identifiant du portefeuille
     * @return flux des transactions
     */
    @GetMapping("/Wallet/{walletId}")
    @Operation(summary = "Get transactions by wallet ID", description = "Retrieves a list of transactions for a given wallet ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public Flux<TransactionResponse> findByWalletId(
            @Parameter(description = "ID of the wallet") @PathVariable("walletId") UUID walletId) {
        return useCase.getTransactionsByWalletId(walletId)
                .map(mapper::toResponse);
    }
}
