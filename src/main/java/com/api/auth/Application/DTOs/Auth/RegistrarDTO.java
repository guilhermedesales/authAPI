package com.api.auth.Application.DTOs.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class RegistrarDTO {

    @NotBlank
    private String nome;
    @Email(message = "Email inválido")
    @NotBlank
    private String email;
    private String senha;

}

