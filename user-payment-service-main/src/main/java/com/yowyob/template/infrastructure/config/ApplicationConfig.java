package com.yowyob.template.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans transverses applicatifs (hachage des mots de passe).
 */
@Configuration
public class ApplicationConfig {

    /**
     * @return encodeur BCrypt pour les secrets utilisateurs
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
