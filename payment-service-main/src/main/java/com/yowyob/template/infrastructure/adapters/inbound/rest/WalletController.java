package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.model.Wallet;
import com.yowyob.template.domain.ports.in.WalletUseCase;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ressource REST {@code /api/v1/wallets} : CRUD et consultations sur les portefeuilles.
 */
@Tag(name = "Wallet Management", description = "API for wallet management")
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletUseCase useCase;
    private final WalletMapper mapper;

    /**
     * Crée un portefeuille pour un propriétaire avec solde initial par défaut côté service.
     *
     * @param requestMono corps validé asynchrone
     * @return réponse 201 avec le portefeuille créé
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new wallet", description = "Creates a new wallet for a user.")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    public Mono<WalletResponse> create(@RequestBody @Valid Mono<WalletRequest> requestMono) {
        return requestMono
                .map(mapper::toDomain)
                .flatMap(useCase::createWallet)
                .map(mapper::toResponse);
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
     * Liste exhaustive des portefeuilles.
     *
     * @return flux de représentations REST
     */
    @GetMapping
    @Operation(summary = "Get all wallets", description = "Retrieves a list of all wallets.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all wallets", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    public Flux<WalletResponse> getAllWallets() {
        return useCase.getAllWallets()
                .map(mapper::toResponse);
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
    public Mono<WalletResponse> getWallet(@Parameter(description = "ID of the wallet to retrieve") @PathVariable UUID id) {
        return useCase.getWalletById(id)
                .map(mapper::toResponse);
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
    public Mono<WalletResponse> getWalletOwner(@Parameter(description = "ID of the wallet owner") @PathVariable UUID id) {
        return useCase.getWalletByOwnerId(id)
                .map(mapper::toResponse);
    }

    /**
     * Met à jour propriétaire et nom (le solde existant est conservé par le cas d’usage).
     *
     * @param id      identifiant du portefeuille
     * @param request champs à fusionner
     * @return représentation mise à jour
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update a wallet", description = "Updates the details of an existing wallet.")
    @ApiResponse(responseCode = "200", description = "Wallet updated successfully", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public Mono<WalletResponse> updateWallet(@Parameter(description = "ID of the wallet to update") @PathVariable UUID id, @RequestBody @Valid WalletRequest request) {
        Wallet wallet = new Wallet(id, request.ownerId(), request.ownerName(), null);
        return useCase.updateWallet(wallet)
                .map(mapper::toResponse);
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