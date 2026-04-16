package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Service.UsuarioSistemaService;
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
@RequestMapping("/usuarioSistema")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
@Tag(name= "Usuario-Sistema", description = "Vinculação de usuários a sistemas e perfis")
public class UsuarioSistemaController {

    private final UsuarioSistemaService usuarioSistemaService;

    public UsuarioSistemaController(UsuarioSistemaService usuarioSistemaService) {
        this.usuarioSistemaService = usuarioSistemaService;
    }

    @PostMapping
    @Operation(
            summary = "Criar vínculo usuário-sistema",
            description = "Vincula um usuário a um sistema com um perfil de acesso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário, sistema ou perfil não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public UsuarioSistemaDTO criar(@RequestBody @Valid CriarUsuarioSistemaDTO dto) {
        log.info("[USUARIO-SISTEMA] Criacao de vinculo iniciada - usuarioId={} sistemaId={} roleId={}", dto.getUsuarioId(), dto.getSistemaId(), dto.getRoleId());
        UsuarioSistemaDTO created = usuarioSistemaService.criar(dto);
        log.info("[USUARIO-SISTEMA] Criacao de vinculo concluida - usuarioSistemaId={}", created.getId());
        return created;
    }

    @GetMapping
    @Operation(
            summary = "Listar vínculos usuário-sistema",
            description = "Lista vínculos com filtros opcionais por usuário e sistema, com paginação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Page<UsuarioSistemaDTO> listar(
            @Parameter(description = "Filtro opcional por usuário")
            @RequestParam(required = false)UUID usuarioId,
            @Parameter(description = "Filtro opcional por sistema")
            @RequestParam(required = false)UUID sistemaId,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam (defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam (defaultValue = "10") int size
    ) {
        log.debug("[USUARIO-SISTEMA] Listagem iniciada - usuarioId={} sistemaId={} page={} size={}", usuarioId, sistemaId, page, size);
        return usuarioSistemaService.listar(usuarioId, sistemaId, page, size);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar vínculo por ID",
            description = "Retorna os detalhes de um vínculo específico entre usuário e sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public UsuarioSistemaDTO  buscarPorId(@PathVariable UUID id) {
        log.debug("[USUARIO-SISTEMA] Busca por id iniciada - usuarioSistemaId={}", id);
        return usuarioSistemaService.buscarPorId(id);
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Alterar perfil do vínculo",
            description = "Atualiza o perfil (role) associado ao vínculo usuário-sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Vínculo ou perfil não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public UsuarioSistemaDTO alterarRole(
            @PathVariable UUID id,
            @Parameter(description = "ID da nova role", required = true)
            @RequestParam UUID roleId
    ) {
        log.info("[USUARIO-SISTEMA] Alteracao de role iniciada - usuarioSistemaId={} novaRoleId={}", id, roleId);
        return usuarioSistemaService.mudarRoleUser(id, roleId);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover vínculo usuário-sistema",
            description = "Exclui o vínculo entre usuário e sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public void deletar(@PathVariable UUID id) {
        log.info("[USUARIO-SISTEMA] Remocao de vinculo iniciada - usuarioSistemaId={}", id);
        usuarioSistemaService.remover(id);
    }
}
