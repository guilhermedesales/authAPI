package com.api.auth.Domain.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String tokenId;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private UserSession session;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private Instant expiryDate;

    @PrePersist
    public void ensureTokenId() {
        if (tokenId == null || tokenId.isBlank()) {
            tokenId = UUID.randomUUID().toString();
        }
    }
}
