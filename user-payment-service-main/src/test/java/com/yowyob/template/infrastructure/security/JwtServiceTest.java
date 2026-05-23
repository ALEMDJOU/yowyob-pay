package com.yowyob.template.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET_BASE64 = "B7lNty52nURS1lCg6KjEvPh6e71c/ndOh1H4mCMRMgo=";

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generateToken_includesAgentClaims() {
        UUID agentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String token = jwtService.generateToken("agent@yowyob.cm", agentId, "Agent Test");

        Claims claims = parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("agent@yowyob.cm");
        assertThat(claims.get(JwtService.CLAIM_AGENT_ID, String.class)).isEqualTo(agentId.toString());
        assertThat(claims.get(JwtService.CLAIM_EMAIL, String.class)).isEqualTo("agent@yowyob.cm");
        assertThat(claims.get(JwtService.CLAIM_NAME, String.class)).isEqualTo("Agent Test");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_BASE64)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
