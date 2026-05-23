package com.yowyob.template.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nom : {@code InternalApiKeyWebFilterTest}
 * <p>
 * Description : tests unitaires de la comparaison de clés via SHA-256 (égalité
 * / inégalité).
 * </p>
 */
class InternalApiKeyWebFilterTest {

    /**
     * Nom : {@code sha256Equals_sameString}
     * <p>
     * Description : deux chaînes identiques produisent la même empreinte et la
     * comparaison renvoie vrai.
     * </p>
     */
    @Test
    @DisplayName("Comparaison SHA-256 : chaînes identiques → true")
    void sha256Equals_sameString() {
        assertTrue(InternalApiKeyWebFilter.sha256ConstantTimeEquals("secret-a", "secret-a"));
    }

    /**
     * Nom : {@code sha256Equals_differentStrings}
     * <p>
     * Description : deux chaînes différentes doivent être rejetées.
     * </p>
     */
    @Test
    @DisplayName("Comparaison SHA-256 : chaînes différentes → false")
    void sha256Equals_differentStrings() {
        assertFalse(InternalApiKeyWebFilter.sha256ConstantTimeEquals("secret-a", "secret-b"));
    }

    /**
     * Nom : {@code sha256Equals_nullHandled}
     * <p>
     * Description : tout argument nul doit conduire à faux sans exception.
     * </p>
     */
    @Test
    @DisplayName("Comparaison SHA-256 : null → false")
    void sha256Equals_nullHandled() {
        assertFalse(InternalApiKeyWebFilter.sha256ConstantTimeEquals(null, "x"));
        assertFalse(InternalApiKeyWebFilter.sha256ConstantTimeEquals("x", null));
    }
}
