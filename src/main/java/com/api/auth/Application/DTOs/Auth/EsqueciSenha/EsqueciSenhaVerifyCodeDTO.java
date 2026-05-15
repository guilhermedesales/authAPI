package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaVerifyCodeDTO {

    @Schema(example = "maria.silva@empresa.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    private String email;

    @Schema(example = "123456")
    @NotBlank(message = "O código é obrigatório")
    private String code;

}

