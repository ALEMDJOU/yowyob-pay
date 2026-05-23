package com.yowyob.template.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.yowyob.template.infrastructure.security.AuthenticationManager;
import com.yowyob.template.infrastructure.security.SecurityContextRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Nom : {@code SecurityConfig}
 * <p>
 * Description : sécurité WebFlux pour le user-payment - JWT via
 * {@link com.yowyob.template.infrastructure.security.SecurityContextRepository},
 * authentification par {@link AuthenticationManager}, CORS piloté par
 * {@link UserPaymentSecurityProperties}.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final AuthenticationManager authenticationManager;
        private final SecurityContextRepository securityContextRepository;
        private final UserPaymentSecurityProperties userPaymentSecurityProperties;

        /**
         * Nom : {@code securityWebFilterChain}
         * <p>
         * Description : autorise l’auth publique, Swagger (si exposé côté springdoc) et
         * Actuator ;
         * le reste exige un contexte de sécurité authentifié.
         * </p>
         *
         * @param http builder fourni par Spring Security
         * @return chaîne de filtres configurée
         */
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                                .authenticationManager(authenticationManager)
                                .securityContextRepository(securityContextRepository)
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                                                .pathMatchers("/actuator/**").permitAll()
                                                .pathMatchers("/swagger-ui.html").permitAll()
                                                .pathMatchers("/swagger-ui/**").permitAll()
                                                .pathMatchers("/v3/api-docs").permitAll()
                                                .pathMatchers("/v3/api-docs/**").permitAll()
                                                .pathMatchers("/webjars/**").permitAll()
                                                .anyExchange().authenticated())
                                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                                                .authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> swe
                                                                .getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
                                                .accessDeniedHandler((swe,
                                                                e) -> Mono.fromRunnable(() -> swe.getResponse()
                                                                                .setStatusCode(HttpStatus.FORBIDDEN))))
                                .build();
        }

        /**
         * Nom : {@code corsConfigurationSource}
         * <p>
         * Description : applique les origines définies dans
         * {@code application.security.cors-allowed-origins-csv}.
         * </p>
         *
         * @return source CORS pour {@code /**}
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(userPaymentSecurityProperties.allowedOriginsList());
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
