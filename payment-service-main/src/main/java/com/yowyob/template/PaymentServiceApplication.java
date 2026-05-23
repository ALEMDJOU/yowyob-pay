package com.yowyob.template;

import com.yowyob.template.infrastructure.config.BusinessProperties;
import com.yowyob.template.infrastructure.config.IdempotencyProperties;
import com.yowyob.template.infrastructure.config.KafkaConsumerRetryProperties;
import com.yowyob.template.infrastructure.config.PaymentProperties;
import com.yowyob.template.infrastructure.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Point d’entrée du microservice de paiement (portefeuilles, transactions,
 * Kafka).
 */
@SpringBootApplication
@EnableConfigurationProperties({ SecurityProperties.class, BusinessProperties.class, PaymentProperties.class,
		IdempotencyProperties.class, KafkaConsumerRetryProperties.class })
public class PaymentServiceApplication {

	/**
	 * Démarre l’application Spring Boot.
	 *
	 * @param args arguments de ligne de commande passés à Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
