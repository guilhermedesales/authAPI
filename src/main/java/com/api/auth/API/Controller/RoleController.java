package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Role.CriarRoleDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.Service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/role")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
@Tag(name= "Role", description = "Gestão de perfis (roles) por sistema")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/criar")
    @Operation(
            summary = "Criar perfil",
            description = "Cria um novo perfil de acesso vinculado a um sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public RoleListDTO criar(@RequestBody @Valid CriarRoleDTO dto) {
        log.info("[ROLE] Criacao de perfil iniciada - sistemaId={} nome={}", dto.getSistemaId(), dto.getNome());
        RoleListDTO created = roleService.criar(dto);
        log.info("[ROLE] Criacao de perfil concluida - roleId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    @Operation(
            summary = "Listar perfis",
            description = "Retorna uma lista paginada de perfis cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Page<RoleListDTO> listar(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.debug("[ROLE] Listagem iniciada - page={} size={}", page, size);
        return roleService.listar(page, size);
    }

    @GetMapping("/buscar/{id}")
    @Operation(
            summary = "Buscar perfil por ID",
            description = "Retorna os detalhes de um perfil específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public RoleDTO buscarPorId(@Parameter(description = "ID do perfil", required = true) @PathVariable UUID id) {
        log.debug("[ROLE] Busca por id iniciada - roleId={}", id);
        return roleService.buscarPorId(id);
    }

}
