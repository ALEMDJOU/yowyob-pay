package com.yowyob.template.infrastructure.adapters.inbound.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vérifie que les payloads Kafka v1 restent compacts (champs attendus
 * uniquement).
 */
class KafkaEventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void walletCreationEventJsonContainsOnlyV1Fields() throws Exception {
        UUID owner = UUID.randomUUID();
        WalletCreationEvent event = new WalletCreationEvent(owner, "Alice");
        String json = mapper.writeValueAsString(event);
        assertTrue(json.contains("\"ownerId\""));
        assertTrue(json.contains("\"ownerName\""));
        assertFalse(json.contains("\"extra\""));
        assertEquals(event, mapper.readValue(json, WalletCreationEvent.class));
    }

    @Test
    void paymentCommissionEventJsonContainsOnlyV1Fields() throws Exception {
        UUID owner = UUID.randomUUID();
        PaymentCommissionEvent event = new PaymentCommissionEvent(owner, new BigDecimal("99.99"));
        String json = mapper.writeValueAsString(event);
        assertTrue(json.contains("\"ownerId\""));
        assertTrue(json.contains("\"baseAmount\""));
        assertFalse(json.contains("\"nested\""));
        PaymentCommissionEvent read = mapper.readValue(json, PaymentCommissionEvent.class);
        assertEquals(0, read.baseAmount().compareTo(event.baseAmount()));
        assertEquals(event.ownerId(), read.ownerId());
    }
}
