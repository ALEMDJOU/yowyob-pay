package com.yowyob.template.infrastructure.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yowyob.template.infrastructure.security.InternalApiKeyWebFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Nom : {@code OpenApiConfig}
 * <p>
 * Description : enregistre le modèle OpenAPI 3 (Springdoc) lorsque
 * {@code application.security.expose-openapi=true},
 * avec schémas de sécurité clé API interne et JWT Bearer (chemins hors API
 * métier).
 * </p>
 */
@Configuration
@ConditionalOnProperty(prefix = "application.security", name = "expose-openapi", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

        /** URL publique exposée dans Swagger (proxy ou localhost selon le profil). */
        @Value("${app.swagger.server-url}")
        private String serverUrl;

        /**
         * Nom : {@code customOpenAPI}
         * <p>
         * Description : agrège métadonnées produit, serveurs et exigences de sécurité
         * pour la documentation interactive.
         * </p>
         *
         * @return modèle OpenAPI enrichi pour springdoc-openapi
         */
        @Bean
        public OpenAPI customOpenAPI() {
                final String bearerAuth = "bearerAuth";
                final String internalApiKey = "internalApiKey";

                return new OpenAPI()
                                .info(new Info()
                                                .title("YowYob Microservice Payment - API")
                                                .version("1.0.0")
                                                .description("Documentation de l'API Payment pour la gestion des Wallets et Transactions. "
                                                                + "Les chemins sous /api/v1/wallets et /api/v1/transactions exigent l'en-tête "
                                                                + InternalApiKeyWebFilter.HEADER_NAME + ".")
                                                .contact(new Contact()
                                                                .name("Équipe Backend")
                                                                .email("dev@yowyob.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("http://springdoc.org")))
                                .servers(List.of(
                                                new Server().url(serverUrl)
                                                                .description("Serveur de Production / Proxy"),
                                                new Server().url("/").description("Serveur Local")))
                                .components(new Components()
                                                .addSecuritySchemes(internalApiKey,
                                                                new SecurityScheme()
                                                                                .name(InternalApiKeyWebFilter.HEADER_NAME)
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.HEADER))
                                                .addSecuritySchemes(bearerAuth,
                                                                new SecurityScheme()
                                                                                .name(bearerAuth)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList(internalApiKey))
                                .addSecurityItem(new SecurityRequirement().addList(bearerAuth));
        }
}
