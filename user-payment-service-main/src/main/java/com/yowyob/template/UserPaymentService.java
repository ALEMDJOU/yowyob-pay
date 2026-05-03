package com.yowyob.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice « user-payment » : agents, authentification JWT et publication des recharges vers Kafka.
 */
@SpringBootApplication
public class UserPaymentService {

    /**
     * @param args arguments passés à Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(UserPaymentService.class, args);
    }

}
