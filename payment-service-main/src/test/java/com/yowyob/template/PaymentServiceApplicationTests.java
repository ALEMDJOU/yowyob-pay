package com.yowyob.template;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de démarrage du contexte complet (désactivé par défaut : nécessite
 * Postgres + Kafka).
 */
@SpringBootTest
@Disabled("Activer uniquement avec l’infra Docker (Postgres + Kafka) pour un test d’intégration complet.")
class PaymentServiceApplicationTests {

    /**
     * Charge le contexte applicatif sans erreur lorsque le test est activé.
     */
    @Test
    void contextLoads() {
    }
}
