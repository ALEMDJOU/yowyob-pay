package com.yowyob.template.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration des écouteurs Kafka : backoff exponentiel sans DLQ ; abandon
 * final journalisé puis acquittement implicite du message fautif.
 * <p>
 * Le bean {@link DefaultErrorHandler} est appliqué automatiquement par Spring
 * Boot
 * à la fabrique {@code kafkaListenerContainerFactory} (injection
 * {@code CommonErrorHandler}).
 */
@Slf4j
@Configuration
public class KafkaListenerErrorConfiguration {

    /**
     * Fabrique un {@link DefaultErrorHandler} avec backoff exponentiel piloté par
     * {@link KafkaConsumerRetryProperties}.
     *
     * @param props paramètres {@code application.kafka.consumer.retry}
     * @return gestionnaire commun des erreurs d’écoute
     */
    @Bean
    public DefaultErrorHandler paymentKafkaDefaultErrorHandler(KafkaConsumerRetryProperties props) {
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(props.initialIntervalMs());
        backOff.setMultiplier(props.multiplier());
        backOff.setMaxInterval(props.maxIntervalMs());
        backOff.setMaxElapsedTime(props.maxElapsedTimeMs());
        return new DefaultErrorHandler((ConsumerRecord<?, ?> record, Exception exception) -> {
            log.error(
                    "Kafka listener : abandon après backoff (sans DLQ) topic={} partition={} offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    exception);
        }, backOff);
    }
}
