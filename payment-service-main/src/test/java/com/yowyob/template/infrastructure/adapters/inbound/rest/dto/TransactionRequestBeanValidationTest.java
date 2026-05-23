package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import com.yowyob.template.domain.model.TransactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nom : {@code TransactionRequestBeanValidationTest}
 * <p>
 * Description : vérifie les contraintes sur {@link TransactionRequest}.
 * </p>
 */
class TransactionRequestBeanValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    /**
     * Nom : {@code initValidator}
     * <p>
     * Description : initialise le validateur Jakarta pour les tests de la classe.
     * </p>
     */
    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Nom : {@code closeFactory}
     * <p>
     * Description : ferme la fabrique de validateurs après tous les tests.
     * </p>
     */
    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    /**
     * Nom : {@code validRequest_hasNoViolations}
     * <p>
     * Description : montant positif et champs requis → aucune violation.
     * </p>
     */
    @Test
    @DisplayName("TransactionRequest valide → aucune violation")
    void validRequest_hasNoViolations() {
        TransactionRequest req = new TransactionRequest(UUID.randomUUID(), new BigDecimal("10.50"),
                TransactionType.PAYMENT);
        assertTrue(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code negativeAmount_isInvalid}
     * <p>
     * Description : un montant négatif viole {@code @Positive}.
     * </p>
     */
    @Test
    @DisplayName("amount négatif → violation")
    void negativeAmount_isInvalid() {
        TransactionRequest req = new TransactionRequest(UUID.randomUUID(), new BigDecimal("-1"),
                TransactionType.PAYMENT);
        assertFalse(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code nullType_isInvalid}
     * <p>
     * Description : un type de transaction absent est interdit.
     * </p>
     */
    @Test
    @DisplayName("type null → violation")
    void nullType_isInvalid() {
        TransactionRequest req = new TransactionRequest(UUID.randomUUID(), BigDecimal.ONE, null);
        assertFalse(validator.validate(req).isEmpty());
    }
}
