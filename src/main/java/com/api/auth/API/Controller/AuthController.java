package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Usuario.CriarUsuarioDTO;
import com.api.auth.Application.Service.UsuarioService;
import com.api.auth.Domain.Entities.Usuario;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public Usuario criar(CriarUsuarioDTO dto) {
        return usuarioService.criar(dto);
    }

}
