package com.yowyob.template.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Génération et validation de jetons JWT (HS256) pour l’authentification des agents.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * @param token jeton Bearer sans préfixe
     * @return sujet (ici l’email) extrait des claims
     * @throws io.jsonwebtoken.JwtException en cas de signature ou format invalide (non documentée ici, propagée)
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
     * @param extraClaims claims additionnels (vide pour l’instant)
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
