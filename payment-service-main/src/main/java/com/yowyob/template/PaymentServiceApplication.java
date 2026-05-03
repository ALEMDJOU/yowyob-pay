package com.yowyob.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d’entrée du microservice de paiement (portefeuilles, transactions,
 * Kafka).
 */
@SpringBootApplication
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
