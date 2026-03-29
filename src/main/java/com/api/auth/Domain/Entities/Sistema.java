package com.api.auth.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sistema extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private String descricao;

    @OneToMany(mappedBy = "sistema")
    private List<Role> roles;
}
