package com.api.auth.Domain.Entities;

import com.api.auth.Domain.Enum.TipoVerificacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VerificationCode {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    private Integer attempts = 0;

    @Enumerated(EnumType.STRING)
    private TipoVerificacao tipo; // LOGIN, ALTERAR_SENHA, etc

    // usados no fluxo de alterar senha
    private String novaSenhaHash;
    @Column(nullable = true)
    private Boolean revogarSessoes; // pode ser null

    // Challenge usado no fluxo de esqueci senha para separar "validar código" de "trocar senha".
    private UUID challengeId;
    private Instant challengeExpiryDate;
    private boolean challengeUsed = false;

    // Vincula challenge de login ao sistema solicitado para evitar ambiguidade multi-sistema.
    private UUID sistemaId;

    // Contexto do pedido para fluxos de step-up.
    private UUID deviceId;
    private String requestIp;
    private String requestUserAgent;
}
