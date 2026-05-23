package com.yowyob.template.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres de backoff pour les écouteurs Kafka (sans DLQ) : exposés sous
 * {@code application.kafka.consumer.retry.*}.
 *
 * @param initialIntervalMs délai initial entre tentatives
 * @param multiplier        facteur multiplicatif du délai
 * @param maxIntervalMs     plafond d’un intervalle
 * @param maxElapsedTimeMs  durée cumulée maximale de tentatives avant abandon
 */
@ConfigurationProperties(prefix = "application.kafka.consumer.retry")
public record KafkaConsumerRetryProperties(
                long initialIntervalMs,
                double multiplier,
                long maxIntervalMs,
                long maxElapsedTimeMs) {
}
