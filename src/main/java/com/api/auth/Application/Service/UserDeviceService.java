package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.DTOs.UserDevice.UserDeviceDTO;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.UserSession;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserDeviceService {

    private final MappingProfile mappingProfile;
    private UserSessionRepository userSessionRepository;

    public UserDeviceService(UserSessionRepository userSessionRepository, MappingProfile mappingProfile) {
        this.userSessionRepository = userSessionRepository;
        this.mappingProfile = mappingProfile;
    }

    public Page<UserDeviceDTO> listar(UUID usuarioId, int page, int size) {
        log.debug("[USER DEVICE] List started - page={} size={}", page, size);

        PageRequest pageable = PageRequest.of(page, size);

        Page<UserSession> result = userSessionRepository.findAllByUsuarioId(usuarioId, pageable);

        log.debug("[USER DEVICE] List success - totalElements={}", result.getTotalElements());
        return result.map(mappingProfile::toDTO);
    }

}
