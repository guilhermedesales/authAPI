package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Permissao.CriarPermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.Service.PermissaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/permissao")
@Tag(name= "Permissão")
public class PermissaoController {

    private final PermissaoService  permissaoService;

    public PermissaoController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @PostMapping("/criar")
    public PermissaoDTO criar(@RequestBody CriarPermissaoDTO dto) {
        return permissaoService.criar(dto);
    }

    @GetMapping("/listar")
    public Page<PermissaoDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return permissaoService.listar(page, size);
    }

    @PutMapping("/editar/{id}")
    public PermissaoDTO editar(@PathVariable UUID id,
                               @RequestBody CriarPermissaoDTO dto) {
        return permissaoService.editar(id, dto);
    }

}
