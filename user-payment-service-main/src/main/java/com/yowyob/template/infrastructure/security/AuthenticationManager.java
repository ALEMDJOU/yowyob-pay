package com.yowyob.template.infrastructure.security;

import com.yowyob.template.domain.ports.out.AgentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentification réactive par email / mot de passe avec validation BCrypt.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final AgentRepositoryPort agentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Charge l’agent par email et vérifie le mot de passe avec l’encodeur.
     *
     * @param authentication jeton contenant email (principal) et mot de passe (credentials)
     * @return authentification réussie avec rôle {@code ROLE_AGENT}
     * @throws BadCredentialsException réactive si utilisateur absent ou mot de passe invalide
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        return agentRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Utilisateur non trouvé")))
                .flatMap(agent -> {
                    if (passwordEncoder.matches(password, agent.password())) {
                        return Mono.just(new UsernamePasswordAuthenticationToken(
                                agent.email(),
                                agent.password(),
                                List.of(new SimpleGrantedAuthority("ROLE_AGENT"))
                        ));
                    } else {
                        return Mono.error(new BadCredentialsException("Mot de passe incorrect"));
                    }
                });
    }
}
