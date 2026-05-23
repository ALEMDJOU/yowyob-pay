package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nom : {@code WalletRequestBeanValidationTest}
 * <p>
 * Description : vérifie les contraintes Bean Validation sur
 * {@link WalletRequest}.
 * </p>
 */
class WalletRequestBeanValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    /**
     * Nom : {@code initValidator}
     * <p>
     * Description : instancie une fabrique de validateurs Jakarta une fois pour la
     * classe de test.
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
     * Description : libère la fabrique de validateurs après l’ensemble des tests.
     * </p>
     */
    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    /**
     * Nom : {@code validRequest_hasNoViolations}
     * <p>
     * Description : un corps cohérent ne doit produire aucune violation.
     * </p>
     */
    @Test
    @DisplayName("WalletRequest valide → aucune violation")
    void validRequest_hasNoViolations() {
        WalletRequest req = new WalletRequest(UUID.randomUUID(), "Alice");
        assertTrue(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code nullOwnerId_isInvalid}
     * <p>
     * Description : un propriétaire absent est rejeté.
     * </p>
     */
    @Test
    @DisplayName("ownerId null → violation")
    void nullOwnerId_isInvalid() {
        WalletRequest req = new WalletRequest(null, "Bob");
        assertFalse(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code blankOwnerName_isInvalid}
     * <p>
     * Description : un nom vide ou blanc est rejeté.
     * </p>
     */
    @Test
    @DisplayName("ownerName vide → violation")
    void blankOwnerName_isInvalid() {
        WalletRequest req = new WalletRequest(UUID.randomUUID(), "   ");
        assertFalse(validator.validate(req).isEmpty());
    }
}
