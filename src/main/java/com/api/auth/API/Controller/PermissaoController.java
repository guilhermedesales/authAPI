package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Permissao.CriarPermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.Service.PermissaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.info("[PERMISSAO] Criar permissao - roleId={} nome={}", dto.getRoleId(), dto.getNome());
        PermissaoDTO created = permissaoService.criar(dto);
        log.info("[PERMISSAO] Permissao criada - permissaoId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    public Page<PermissaoDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("[PERMISSAO] Listar permissoes - page={} size={}", page, size);
        return permissaoService.listar(page, size);
    }

    @PutMapping("/editar/{id}")
    public PermissaoDTO editar(@PathVariable UUID id,
                               @RequestBody CriarPermissaoDTO dto) {
        log.info("[PERMISSAO] Editar permissao - permissaoId={} roleId={}", id, dto.getRoleId());
        PermissaoDTO updated = permissaoService.editar(id, dto);
        log.info("[PERMISSAO] Permissao editada - permissaoId={}", updated.getId());
        return updated;
    }

}
