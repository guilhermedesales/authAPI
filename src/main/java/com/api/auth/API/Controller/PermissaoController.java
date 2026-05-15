package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Permissao.CriarPermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.Service.PermissaoService;
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
@RequestMapping("/permissao")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
@Tag(name= "Permissao", description = "Gestão de permissões de acesso")
public class PermissaoController {

    private final PermissaoService  permissaoService;

    public PermissaoController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @PostMapping("/criar")
    @Operation(
            summary = "Criar permissão",
            description = "Cria uma nova permissão vinculada a um perfil (role)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public PermissaoDTO criar(@RequestBody @Valid CriarPermissaoDTO dto) {
        log.info("[PERMISSAO] Criacao iniciada - roleId={} nome={}", dto.getRoleId(), dto.getNome());
        PermissaoDTO created = permissaoService.criar(dto);
        log.info("[PERMISSAO] Criacao concluida - permissaoId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    @Operation(
            summary = "Listar permissões",
            description = "Retorna lista paginada de permissões cadastradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Page<PermissaoDTO> listar(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.debug("[PERMISSAO] Listagem iniciada - page={} size={}", page, size);
        return permissaoService.listar(page, size);
    }

    @PutMapping("/editar/{id}")
    @Operation(
            summary = "Editar permissão",
            description = "Atualiza dados de uma permissão existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public PermissaoDTO editar(@PathVariable UUID id,
                               @RequestBody @Valid CriarPermissaoDTO dto) {
        log.info("[PERMISSAO] Edicao iniciada - permissaoId={} roleId={}", id, dto.getRoleId());
        PermissaoDTO updated = permissaoService.editar(id, dto);
        log.info("[PERMISSAO] Edicao concluida - permissaoId={}", updated.getId());
        return updated;
    }

}
