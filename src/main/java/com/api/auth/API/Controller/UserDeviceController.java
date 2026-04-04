package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.UserDevice.UserDeviceDTO;
import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Service.UserDeviceService;
import com.api.auth.Application.Utils.ErrorMessages;
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
@Tag(name= "UserDevice")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public  UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @GetMapping("/listar")
    public Page<UserDeviceDTO> listar(
            @AuthenticationPrincipal String usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        UUID authenticatedUserId;
        try {
            authenticatedUserId = UUID.fromString(usuarioId);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        log.debug("[USER DEVICE] List - userId={} page={} size={}", authenticatedUserId, page, size);

        return userDeviceService.listar(authenticatedUserId, page, size);
    }

}
