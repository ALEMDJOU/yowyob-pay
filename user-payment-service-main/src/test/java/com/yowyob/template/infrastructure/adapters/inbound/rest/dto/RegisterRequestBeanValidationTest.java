package com.yowyob.template.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nom : {@code RegisterRequestBeanValidationTest}
 * <p>
 * Description : contrôle les annotations de validation sur
 * {@link RegisterRequest}.
 * </p>
 */
class RegisterRequestBeanValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    /**
     * Nom : {@code initValidator}
     * <p>
     * Description : prépare le validateur Jakarta avant les tests.
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
     * Description : ferme la fabrique après les tests.
     * </p>
     */
    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    /**
     * Nom : {@code validRequest_hasNoViolations}
     * <p>
     * Description : inscription cohérente → aucune violation.
     * </p>
     */
    @Test
    @DisplayName("RegisterRequest valide → aucune violation")
    void validRequest_hasNoViolations() {
        RegisterRequest req = new RegisterRequest("Alice", "alice@example.com", "password12");
        assertTrue(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code shortPassword_isInvalid}
     * <p>
     * Description : mot de passe trop court.
     * </p>
     */
    @Test
    @DisplayName("mot de passe trop court → violation")
    void shortPassword_isInvalid() {
        RegisterRequest req = new RegisterRequest("Bob", "bob@example.com", "short");
        assertFalse(validator.validate(req).isEmpty());
    }

    /**
     * Nom : {@code invalidEmail_isInvalid}
     * <p>
     * Description : email mal formé.
     * </p>
     */
    @Test
    @DisplayName("email invalide → violation")
    void invalidEmail_isInvalid() {
        RegisterRequest req = new RegisterRequest("Bob", "not-an-email", "password12");
        assertFalse(validator.validate(req).isEmpty());
    }
}
