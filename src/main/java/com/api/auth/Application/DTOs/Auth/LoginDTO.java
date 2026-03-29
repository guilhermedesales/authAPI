package com.api.auth.Application.DTOs.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginDTO {

    private String email;
    private String senha;
    private UUID sistemaId;

}
