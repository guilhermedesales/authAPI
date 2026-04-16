package com.api.auth.Application.DTOs.Usuario;

import jakarta.validation.constraints.NotBlank;

public class AtualizarUsuarioDTO {

    @NotBlank
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}


