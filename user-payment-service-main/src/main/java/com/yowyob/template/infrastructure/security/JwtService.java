package com.yowyob.template.infrastructure.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Génération et validation de jetons JWT (HS256) pour l’authentification des
 * agents.
 */
@Service
public class JwtService {

    public static final String CLAIM_AGENT_ID = "agentId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_NAME = "name";

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * @param token jeton Bearer sans préfixe
     * @return sujet (ici l’email) extrait des claims
     * @throws io.jsonwebtoken.JwtException en cas de signature ou format invalide
     *                                      (non documentée ici, propagée)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * @param username identifiant à inscrire comme sujet du jeton
     * @return chaîne JWT compacte
     */
    public String generateToken(String username) {
        return buildToken(new HashMap<>(), username);
    }

    /**
     * @param email   sujet du jeton (identifiant de connexion)
     * @param agentId identifiant métier de l’agent
     * @param name    nom affiché
     * @return JWT avec claims {@code agentId}, {@code email} et {@code name}
     */
    public String generateToken(String email, UUID agentId, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_AGENT_ID, agentId.toString());
        claims.put(CLAIM_EMAIL, email);
        claims.put(CLAIM_NAME, name);
        return buildToken(claims, email);
    }

    /**
     * @param token jeton Bearer
     * @return claim {@code agentId} ou {@code null} si absent
     */
    public UUID extractAgentId(String token) {
        String raw = extractAllClaims(token).get(CLAIM_AGENT_ID, String.class);
        return raw != null ? UUID.fromString(raw) : null;
    }

    /**
     * @param token jeton Bearer
     * @return claim {@code name} ou {@code null}
     */
    public String extractName(String token) {
        return extractAllClaims(token).get(CLAIM_NAME, String.class);
    }

    /**
     * @param extraClaims claims additionnels
     * @param username    sujet du jeton
     * @return JWT signé avec expiration configurée
     */
    private String buildToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * @param token jeton à contrôler
     * @return {@code false} si parsing ou expiration échoue
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param token jeton à analyser
     * @return {@code true} si la date d’expiration est dans le passé
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * @param token JWT brut
     * @return ensemble des claims parsés
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * @return clé symétrique dérivée du secret encodé Base64
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
