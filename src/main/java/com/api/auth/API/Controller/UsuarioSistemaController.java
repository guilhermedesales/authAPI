package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Service.UsuarioSistemaService;
import com.api.auth.Domain.Entities.UsuarioSistema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.info("[USUARIO_SISTEMA] Criar vinculo - usuarioId={} sistemaId={} roleId={}", dto.getUsuarioId(), dto.getSistemaId(), dto.getRoleId());
        UsuarioSistemaDTO created = usuarioSistemaService.criar(dto);
        log.info("[USUARIO_SISTEMA] Vinculo criado - usuarioSistemaId={}", created.getId());
        return created;
    }

    @GetMapping
    public Page<UsuarioSistemaDTO> listar(
            @RequestParam(required = false)UUID usuarioId,
            @RequestParam(required = false)UUID sistemaId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ) {
        log.debug("[USUARIO_SISTEMA] Listar vinculos - usuarioId={} sistemaId={} page={} size={}", usuarioId, sistemaId, page, size);
        return usuarioSistemaService.listar(usuarioId, sistemaId, page, size);
    }

    @GetMapping("/{id}")
    public UsuarioSistemaDTO  buscarPorId(@PathVariable UUID id) {
        log.debug("[USUARIO_SISTEMA] Buscar vinculo - usuarioSistemaId={}", id);
        return usuarioSistemaService.buscarPorId(id);
    }

    @PatchMapping("/{id}/role")
    public UsuarioSistemaDTO mudarRole(
            @PathVariable UUID id,
            @RequestParam UUID roleId
    ) {
        log.info("[USUARIO_SISTEMA] Alterar role - usuarioSistemaId={} novaRoleId={}", id, roleId);
        return usuarioSistemaService.mudarRoleUser(id, roleId);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        log.info("[USUARIO_SISTEMA] Remover vinculo - usuarioSistemaId={}", id);
        usuarioSistemaService.remover(id);
    }
}
