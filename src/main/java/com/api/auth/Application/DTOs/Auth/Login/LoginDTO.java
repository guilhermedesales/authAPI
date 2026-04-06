package com.api.auth.Application.DTOs.Auth.Login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginDTO {

    @Schema(example = "maria.silva@empresa.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    private String email;

    @Schema(example = "Senha@123")
    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    @NotNull(message = "O sistemaId é obrigatório")
    private UUID sistemaId;

}
