package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Role.CriarRoleDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.Service.RoleService;
import com.api.auth.Domain.Entities.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        return roleService.criar(dto);
    }

    @GetMapping("/listar")
    public Page<RoleListDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return roleService.listar(page, size);
    }

    @GetMapping("/buscar/{id}")
    public RoleDTO buscar(@RequestParam UUID id) {
        return roleService.buscarPorId(id);
    }

}
