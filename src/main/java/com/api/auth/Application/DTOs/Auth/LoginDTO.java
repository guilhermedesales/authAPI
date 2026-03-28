package com.api.auth.Application.DTOs.Auth;

import java.util.UUID;

public class LoginDTO {

    private String email;
    private String senha;
    private UUID sistemaId;

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getSenha() {return senha;}
    public void setSenha(String senha) {this.senha = senha;}

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

}
