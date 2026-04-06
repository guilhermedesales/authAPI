package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.UserDevice.UserDeviceDTO;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Domain.Entities.UserSession;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserDeviceService {

    private final MappingProfile mappingProfile;
    private final UserSessionRepository userSessionRepository;

    public UserDeviceService(UserSessionRepository userSessionRepository, MappingProfile mappingProfile) {
        this.userSessionRepository = userSessionRepository;
        this.mappingProfile = mappingProfile;
    }

    public Page<UserDeviceDTO> listar(UUID usuarioId, int page, int size) {
        log.debug("[DISPOSITIVO] Listagem iniciada - usuarioId={} page={} size={}", usuarioId, page, size);

        PageRequest pageable = PageRequest.of(page, size);

        Page<UserSession> resultado = userSessionRepository.findAllByUsuarioId(usuarioId, pageable);

        log.debug("[DISPOSITIVO] Listagem concluida - totalElementos={}", resultado.getTotalElements());
        return resultado.map(mappingProfile::toDTO);
    }

}
