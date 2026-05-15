package com.api.auth.Application.DTOs.Sistema;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CriarSistemaDTO {

    @Schema(example = "Portal Administrativo")
    @NotBlank(message = "O nome do sistema é obrigatório")
    private String nome;

    @Schema(example = "Sistema interno para gestão de usuários e permissões")
    @NotBlank(message = "A descrição do sistema é obrigatória")
    private String descricao;

}
