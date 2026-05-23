package com.yowyob.template.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.DefaultErrorHandler;

/**
 * Vérifie que la fabrique d’{@link DefaultErrorHandler} Kafka est bien
 * instanciable avec les propriétés de backoff.
 */
class KafkaListenerErrorConfigurationTest {

    @Test
    void createsDefaultErrorHandlerFromRetryProperties() {
        KafkaConsumerRetryProperties props = new KafkaConsumerRetryProperties(200L, 2.0, 8000L, 45_000L);
        KafkaListenerErrorConfiguration configuration = new KafkaListenerErrorConfiguration();
        DefaultErrorHandler handler = configuration.paymentKafkaDefaultErrorHandler(props);
        assertThat(handler).isNotNull();
    }
}
