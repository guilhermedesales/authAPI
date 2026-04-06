package com.api.auth.Application.DTOs.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CriarRoleDTO {

    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    @NotNull(message = "O sistemaId é obrigatório")
    private UUID sistemaId;

    @Schema(example = "ADMIN")
    @NotBlank(message = "O nome da role é obrigatório")
    private String nome;

    @Schema(example = "Perfil com acesso administrativo completo")
    @NotBlank(message = "A descrição da role é obrigatória")
    private String descricao;

}
