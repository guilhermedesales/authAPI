package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Auth.LoginDTO;
import com.api.auth.Application.DTOs.Auth.RegistrarDTO;
import com.api.auth.Application.DTOs.Usuario.CriarUsuarioDTO;
import com.api.auth.Application.Service.AuthService;
import com.api.auth.Application.Service.UsuarioService;
import com.api.auth.Domain.Entities.Usuario;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public AuthController(UsuarioService usuarioService, AuthService authService){
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public Usuario registrar(@Valid @RequestBody RegistrarDTO dto) {
        return authService.registrar(dto);
    }

    @PostMapping("login")
    public String login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

}
