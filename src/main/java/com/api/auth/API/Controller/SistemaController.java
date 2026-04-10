package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Sistema.CriarSistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.Service.SistemaService;
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
@RequestMapping("/sistema")
@PreAuthorize("hasAuthority('ROLE_GLOBAL_ADMIN')")
@Tag(name= "Sistema", description = "Gestão de sistemas/clientes da plataforma")
public class SistemaController {

    private final SistemaService sistemaService;

    public SistemaController(SistemaService sistemaService) {
        this.sistemaService = sistemaService;
    }

    @PostMapping("/criar")
    @Operation(
            summary = "Criar sistema",
            description = "Cria um novo sistema para vinculação de usuários, perfis e permissões."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sistema criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public SistemaListDTO criar(@RequestBody @Valid CriarSistemaDTO dto) {
        log.info("[SISTEMA] Criacao iniciada - nome={}", dto.getNome());
        SistemaListDTO created = sistemaService.criar(dto);
        log.info("[SISTEMA] Criacao concluida - sistemaId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    @Operation(
            summary = "Listar sistemas",
            description = "Retorna lista paginada de sistemas cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Page<SistemaListDTO> listar(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam (defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam (defaultValue = "10") int size
    ){
        log.debug("[SISTEMA] Listagem iniciada - page={} size={}", page, size);
        return sistemaService.listar(page, size);
    }

    @GetMapping("/buscar/{id}")
    @Operation(
            summary = "Buscar sistema por ID",
            description = "Retorna os dados detalhados de um sistema específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sistema encontrado"),
            @ApiResponse(responseCode = "404", description = "Sistema não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public SistemaDTO buscarPorId(@Parameter(description = "ID do sistema", required = true) @PathVariable UUID id) {
        log.debug("[SISTEMA] Busca por id iniciada - sistemaId={}", id);
        return sistemaService.buscarPorId(id);
    }

}
