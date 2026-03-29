package com.api.auth.Application.DTOs.Permissao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PermissaoResumoDTO {

    private UUID id;
    private String nome;

}
