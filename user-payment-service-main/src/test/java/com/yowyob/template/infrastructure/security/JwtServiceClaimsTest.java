package com.yowyob.template.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceClaimsTest {

    private static final String SECRET_BASE64 = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes());

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generateToken_includesAgentIdEmailAndNameClaims() {
        UUID agentId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        String email = "agent@yowyob.test";
        String name = "Agent Test";

        String token = jwtService.generateToken(email, agentId, name);

        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        assertThat(jwtService.extractAgentId(token)).isEqualTo(agentId);
        assertThat(jwtService.extractName(token)).isEqualTo(name);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtService.generateToken(
                "x@y.com", UUID.randomUUID(), "X");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
