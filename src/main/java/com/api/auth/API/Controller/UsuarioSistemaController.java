package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Service.UsuarioSistemaService;
import com.api.auth.Domain.Entities.UsuarioSistema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarioSistema")
@Tag(name= "Usuario Sistema")
public class UsuarioSistemaController {

    private final UsuarioSistemaService usuarioSistemaService;

    public UsuarioSistemaController(UsuarioSistemaService usuarioSistemaService) {
        this.usuarioSistemaService = usuarioSistemaService;
    }

    @PostMapping
    public UsuarioSistemaDTO criar(CriarUsuarioSistemaDTO dto) {
        return usuarioSistemaService.criar(dto);
    }

}
