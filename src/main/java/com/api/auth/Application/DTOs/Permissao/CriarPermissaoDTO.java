package com.api.auth.Application.DTOs.Permissao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CriarPermissaoDTO {

    @Schema(example = "USUARIO_LISTAR")
    @NotBlank(message = "O nome da permissão é obrigatório")
    private  String nome;

    @Schema(example = "Permite listar usuários no sistema")
    @NotBlank(message = "A descrição da permissão é obrigatória")
    private  String descricao;

    @Schema(example = "2a56b034-df9c-4ab6-95ba-bec7c8b6d900")
    @NotNull(message = "O roleId é obrigatório")
    private UUID roleId;

}
