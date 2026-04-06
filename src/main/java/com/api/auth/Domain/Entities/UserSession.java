package com.api.auth.Domain.Entities;

import com.api.auth.Domain.Enum.SessionTrustLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UserSession extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID deviceId; // id salvo no front (local storage, cookies, etc..)

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sistema_id")
    private Sistema sistema;

    private String ip;

    private String deviceName;
    private String location;
    @Enumerated(EnumType.STRING)
    private SessionTrustLevel trustLevel;
    private Integer riskScore;
    private String riskSignals;
    private Instant revokedAt;
    private Instant lastUsedAt;

}
