package com.api.auth.Application.DTOs.UsuarioSistema;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CriarUsuarioSistemaDTO {

    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    @NotNull(message = "O sistemaId é obrigatório")
    private UUID sistemaId;

    @Schema(example = "2a56b034-df9c-4ab6-95ba-bec7c8b6d900")
    @NotNull(message = "O roleId é obrigatório")
    private UUID roleId;

    @Schema(example = "0d5ca9ea-7f8e-4dbb-9113-aa0b3cf87d2b")
    @NotNull(message = "O usuarioId é obrigatório")
    private UUID usuarioId;

}
