package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UserDevice.UserDeviceDTO;
import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Service.UserDeviceService;
import com.api.auth.Application.Utils.ErrorMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user-device")
@Tag(name= "Dispositivo", description = "Consulta de dispositivos/sessões do usuário autenticado")
public class UserDeviceController {

    private final UserDeviceService dispositivoUsuarioService;

    public  UserDeviceController(UserDeviceService userDeviceService) {
        this.dispositivoUsuarioService = userDeviceService;
    }

    @GetMapping("/listar")
    @Operation(
            summary = "Listar dispositivos do usuário",
            description = "Retorna os dispositivos/sessões do usuário autenticado com paginação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Sessão inválida ou expirada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public Page<UserDeviceDTO> listarDispositivos(
            @Parameter(description = "ID do usuário autenticado no token JWT", hidden = true)
            @AuthenticationPrincipal String usuarioId,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        UUID usuarioAutenticadoId;
        try {
            usuarioAutenticadoId = UUID.fromString(usuarioId);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        log.debug("[DISPOSITIVO] Listagem iniciada - usuarioId={} page={} size={}", usuarioAutenticadoId, page, size);

        return dispositivoUsuarioService.listar(usuarioAutenticadoId, page, size);
    }

}
