package com.api.auth.API.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Endpoints de verificação de disponibilidade e autenticação")
public class HealthController {

    @GetMapping("/validateJWT")
    @Operation(
            summary = "Validar autenticação JWT",
            description = "Endpoint simples para validar se uma requisição autenticada com JWT consegue acessar recursos protegidos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT válido"),
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Map<String, String> validarToken() {
        log.debug("[HEALTH] Validacao de JWT acessada");
        return Map.of("mensagem", "JWT funcionando");
    }
}