package com.yowyob.template.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reconstruction du contexte de sécurité à partir de l’en-tête {@code Authorization: Bearer …}.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtService jwtService;

    /**
     * Non implémenté : pas de persistance serveur du contexte.
     *
     * @throws UnsupportedOperationException toujours
     */
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Lit le JWT, valide et construit un {@link SecurityContext} si valide.
     *
     * @param exchange requête entrante
     * @return contexte authentifié ou vide si pas de Bearer / jeton invalide
     */
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .flatMap(authHeader -> {
                    String token = authHeader.substring(7);

                    if (jwtService.isTokenValid(token)) {
                        String username = jwtService.extractUsername(token);

                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                username,
                                token,
                                List.of(new SimpleGrantedAuthority("ROLE_AGENT"))
                        );
                        return Mono.just(new SecurityContextImpl(auth));
                    }
                    return Mono.empty();
                });
    }
}
