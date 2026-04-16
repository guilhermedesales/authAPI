package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Usuario.AtualizarUsuarioDTO;
import com.api.auth.Application.DTOs.Usuario.UsuarioDTO;
import com.api.auth.Application.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/usuario")
@Tag(name= "Usuario", description = "Gestão de usuarios da plataforma")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/listar")
    @Operation(
            summary = "Listar usuarios",
            description = "Retorna lista paginada de usuarios registrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
    public Page<UsuarioDTO> listar(
            @Parameter(description = "Filtro opcional por sistema")
            @RequestParam(required = false) UUID sistemaId,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam (defaultValue = "10") int size
    ){
        log.debug("[USUARIO] Listagem iniciada - page={} size={}", page, size);
        return usuarioService.listar(sistemaId, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por id", description = "Global admin vê todos, admin vê apenas do próprio sistema e usuário comum vê apenas o próprio cadastro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
    public UsuarioDTO buscarPorId(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id
    ) {
        log.debug("[USUARIO] Busca por id iniciada - usuarioId={}", id);
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuario", description = "Aplica as mesmas regras de escopo do GET por id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_GLOBAL_ADMIN')")
    public UsuarioDTO atualizar(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUsuarioDTO dto
    ) {
        log.info("[USUARIO] Atualizacao iniciada - usuarioId={}", id);
        return usuarioService.atualizar(id, dto);
    }

}
