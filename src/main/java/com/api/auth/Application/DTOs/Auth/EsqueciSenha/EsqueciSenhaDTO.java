package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaDTO {
    @Schema(example = "maria.silva@empresa.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    private String email;
}
