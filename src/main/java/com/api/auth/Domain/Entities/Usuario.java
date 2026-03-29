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
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    @Column(unique = true)
    private String email;
    private String senha;
    private boolean ativo;

}
