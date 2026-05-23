package com.yowyob.template.infrastructure.adapters.outbound.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ligne {@code idempotency_requests} : rejouer une réponse HTTP identique pour
 * une même clé et un même corps (empreinte).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("idempotency_requests")
public class IdempotencyRequestEntity {

    @Id
    private UUID id;

    private String scope;

    @Column("idempotency_key_hash")
    private String idempotencyKeyHash;

    @Column("request_fingerprint")
    private String requestFingerprint;

    @Column("http_status")
    private int httpStatus;

    @Column("response_body")
    private String responseBody;

    @Column("created_at")
    private Instant createdAt;
}
