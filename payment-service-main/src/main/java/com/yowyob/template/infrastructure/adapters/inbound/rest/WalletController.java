package com.yowyob.template.infrastructure.adapters.inbound.rest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yowyob.template.application.service.IdempotencyService;
import com.yowyob.template.domain.exception.MissingIdempotencyKeyException;
import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.model.WalletPage;
import com.yowyob.template.domain.model.idempotency.IdempotencyContext;
import com.yowyob.template.domain.ports.in.WalletUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.PagedWalletsResponse;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.WalletResponse;
import com.yowyob.template.infrastructure.mappers.WalletMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;

/**
 * Ressource REST {@code /api/v1/wallets} : CRUD et consultations sur les
 * portefeuilles.
 */
@Tag(name = "Wallet Management", description = "API for wallet management")
@RestController
@RequestMapping("/api/v1/wallets")
@Validated
@RequiredArgsConstructor
public class WalletController {

    private final WalletUseCase useCase;
    private final WalletMapper mapper;
    private final IdempotencyService idempotencyService;

    /**
     * Crée un portefeuille pour un propriétaire avec solde initial par défaut côté
     * service.
     *
     * @param idempotencyKey en-tête obligatoire d’idempotence
     * @param requestMono    corps validé asynchrone
     * @return réponse 201 (ou rejouée 201 avec le même corps)
     */
    @PostMapping
    @Operation(summary = "Create a new wallet", description = "Creates a new wallet for a user. Requires Idempotency-Key.")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing Idempotency-Key or invalid key")
    @ApiResponse(responseCode = "409", description = "Idempotency key reused with a different request body")
    public Mono<ResponseEntity<WalletResponse>> create(
            @Parameter(description = "Client-provided idempotency token", required = true) @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid Mono<WalletRequest> requestMono) {
        return requireIdempotencyKey(idempotencyKey)
                .then(requestMono.flatMap(request -> {
                    try {
                        String fp = idempotencyService.fingerprintForRequestBody(request);
                        return useCase.createWalletWithIdempotency(
                                mapper.toDomain(request),
                                Optional.of(new IdempotencyContext(idempotencyKey, fp)))
                                .map(out -> ResponseEntity.status(out.httpStatus())
                                        .body(mapper.toResponse(out.value())));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new IllegalStateException(e));
                    }
                }));
    }

    private Mono<Void> requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.error(new MissingIdempotencyKeyException(
                    "L’en-tête Idempotency-Key est obligatoire pour cette opération"));
        }
        return Mono.empty();
    }

    /**
     * Indique si le solde du portefeuille est strictement positif.
     *
     * @param id identifiant du portefeuille
     * @return {@code true} si opération possible au sens du solde
     */
    @GetMapping("/{id}/can-operate")
    public Mono<Boolean> canOperate(@PathVariable UUID id) {
        return useCase.getWalletById(id)
                .map(wallet -> wallet.balance().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Liste paginée des portefeuilles (paramètres {@code page} et {@code size}
     * obligatoires).
     *
     * @param page index 0-based
     * @param size taille de page entre 1 et 50
     * @return enveloppe paginée
     */
    @GetMapping
    @Operation(summary = "List wallets (paginated)", description = "Retrieves wallets with mandatory query parameters page (>=0) and size (1..50).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved page", content = @Content(schema = @Schema(implementation = PagedWalletsResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing or invalid pagination parameters")
    public Mono<PagedWalletsResponse> getWalletsPage(
            @Parameter(description = "Page index (0-based)", required = true, example = "0") @RequestParam("page") @NotNull @Min(0) Integer page,
            @Parameter(description = "Page size (1-50)", required = true, example = "20") @RequestParam("size") @NotNull @Min(1) @Max(50) Integer size) {
        return useCase.getWalletsPage(page, size).map(this::toPagedWalletsResponse);
    }

    private PagedWalletsResponse toPagedWalletsResponse(WalletPage walletPage) {
        return new PagedWalletsResponse(
                walletPage.content().stream().map(mapper::toResponse).toList(),
                walletPage.page(),
                walletPage.size(),
                walletPage.totalElements(),
                walletPage.totalPages());
    }

    /**
     * Détail d’un portefeuille par identifiant technique.
     *
     * @param id clé UUID
     * @return portefeuille ou erreur 404 via exception handler global si absent
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID", description = "Retrieves wallet details by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public Mono<WalletResponse> getWallet(
            @Parameter(description = "ID of the wallet to retrieve") @PathVariable UUID id) {
        return useCase.getWalletById(id).map(mapper::toResponse);
    }

    /**
     * Recherche par identifiant de propriétaire métier.
     *
     * @param id owner UUID
     * @return portefeuille associé
     */
    @GetMapping("/owner/{id}")
    @Operation(summary = "Get wallet by owner ID", description = "Retrieves wallet details by the owner's unique ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    @ApiResponse(responseCode = "404", description = "Wallet not found for the given owner")
    public Mono<WalletResponse> getWalletOwner(
            @Parameter(description = "ID of the wallet owner") @PathVariable UUID id) {
        return useCase.getWalletByOwnerId(id).map(mapper::toResponse);
    }

    /**
     * Met à jour propriétaire et nom (le solde existant est conservé par le cas
     * d’usage).
     *
     * @param idempotencyKey en-tête obligatoire d’idempotence
     * @param id             identifiant du portefeuille
     * @param request        champs à fusionner
     * @return représentation mise à jour (200 ou rejouée 200)
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update a wallet", description = "Updates the details of an existing wallet. Requires Idempotency-Key.")
    @ApiResponse(responseCode = "200", description = "Wallet updated successfully", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing Idempotency-Key or invalid key")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    @ApiResponse(responseCode = "409", description = "Idempotency key reused with a different request body")
    public Mono<ResponseEntity<WalletResponse>> updateWallet(
            @Parameter(description = "Client-provided idempotency token", required = true) @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(description = "ID of the wallet to update") @PathVariable UUID id,
            @RequestBody @Valid WalletRequest request) {
        return requireIdempotencyKey(idempotencyKey).then(Mono.defer(() -> {
            try {
                String fp = idempotencyService.fingerprintForWalletUpdate(id, request);
                Wallet wallet = new Wallet(id, request.ownerId(), request.ownerName(), null);
                return useCase
                        .updateWalletWithIdempotency(wallet, Optional.of(new IdempotencyContext(idempotencyKey, fp)))
                        .map(out -> ResponseEntity.status(out.httpStatus()).body(mapper.toResponse(out.value())));
            } catch (JsonProcessingException e) {
                return Mono.error(new IllegalStateException(e));
            }
        }));
    }

    /**
     * Suppression définitive du portefeuille.
     *
     * @param id identifiant à supprimer
     * @return vide avec statut 204
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a wallet", description = "Deletes a wallet by its unique ID.")
    @ApiResponse(responseCode = "204", description = "Wallet deleted successfully")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public Mono<Void> deleteWallet(@Parameter(description = "ID of the wallet to delete") @PathVariable UUID id) {
        return useCase.deleteWallet(id);
    }
}
