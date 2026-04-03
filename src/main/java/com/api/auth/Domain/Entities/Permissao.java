package com.api.auth.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Permissao extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
