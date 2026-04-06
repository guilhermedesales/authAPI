package com.api.auth.Application.DTOs.Auth.Registrar;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrarDTO {

    @Schema(example = "Maria Silva")
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Schema(example = "maria.silva@empresa.com")
    @Email(message = "O email informado é inválido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @Schema(example = "Senha@123")
    @NotBlank(message = "A senha é obrigatória")
    private String senha;

}

