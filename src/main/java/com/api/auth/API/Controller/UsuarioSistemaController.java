package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Service.UsuarioSistemaService;
import com.api.auth.Domain.Entities.UsuarioSistema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public Page<UsuarioSistemaDTO> listar(
            @RequestParam(required = false)UUID usuarioId,
            @RequestParam(required = false)UUID sistemaId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ) {
        return usuarioSistemaService.listar(usuarioId, sistemaId, page, size);
    }
}
