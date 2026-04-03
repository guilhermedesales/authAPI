package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Role.CriarRoleDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.Service.RoleService;
import com.api.auth.Domain.Entities.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/role")
@Tag(name= "Roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/criar")
    public RoleListDTO criar(CriarRoleDTO dto) {
        log.info("[ROLE] Criar role - sistemaId={} nome={}", dto.getSistemaId(), dto.getNome());
        RoleListDTO created = roleService.criar(dto);
        log.info("[ROLE] Role criada - roleId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    public Page<RoleListDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("[ROLE] Listar roles - page={} size={}", page, size);
        return roleService.listar(page, size);
    }

    @GetMapping("/buscar/{id}")
    public RoleDTO buscar(@RequestParam UUID id) {
        log.debug("[ROLE] Buscar role - roleId={}", id);
        return roleService.buscarPorId(id);
    }

}
