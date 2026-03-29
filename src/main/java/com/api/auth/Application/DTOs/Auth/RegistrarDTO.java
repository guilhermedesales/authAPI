package com.api.auth.Application.DTOs.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class RegistrarDTO {

    private String nome;
    private String email;
    private String senha;

}

