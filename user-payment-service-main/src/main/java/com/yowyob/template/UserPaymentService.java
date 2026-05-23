package com.yowyob.template;

import com.yowyob.template.infrastructure.config.UserBusinessProperties;
import com.yowyob.template.infrastructure.config.UserPaymentSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Microservice « user-payment » : agents, authentification JWT et publication
 * des recharges vers Kafka.
 */
@SpringBootApplication
@EnableConfigurationProperties({ UserPaymentSecurityProperties.class, UserBusinessProperties.class })
public class UserPaymentService {

    /**
     * @param args arguments passés à Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(UserPaymentService.class, args);
    }

}
