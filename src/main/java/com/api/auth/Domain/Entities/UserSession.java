package com.api.auth.Domain.Entities;

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
    private Instant revokedAt;
    private Instant lastUsedAt;

}
