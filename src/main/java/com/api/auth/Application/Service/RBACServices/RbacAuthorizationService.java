package com.api.auth.Application.Service.RBACServices;

import com.api.auth.Application.Exceptions.ForbiddenException;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Domain.Entities.Role;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class RbacAuthorizationService {

    public boolean isGlobalAdmin() {
        return hasAuthority("ROLE_GLOBAL_ADMIN");
    }

    public boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }

    public UUID getCurrentUsuarioId() {
        try {
            return UUID.fromString(getAuthentication().getName());
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
        }
    }

    public UUID getCurrentSistemaId() {
        Object details = getAuthentication().getDetails();
        if (!(details instanceof Claims claims)) {
            return null;
        }

        Object sistemaId = claims.get("sistemaId");
        if (sistemaId == null) {
            return null;
        }

        try {
            return UUID.fromString(String.valueOf(sistemaId));
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
        }
    }

    public void validateCanManageSistema(UUID sistemaId) {
        if (sistemaId == null) {
            throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
        }

        if (isGlobalAdmin()) {
            return;
        }

        UUID currentSistemaId = getCurrentSistemaId();
        if (!Objects.equals(currentSistemaId, sistemaId)) {
            throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
        }
    }

    public void validateCanManageRole(Role role) {
        if (role == null || role.getSistema() == null) {
            if (!isGlobalAdmin()) {
                throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
            }
            return;
        }

        validateCanManageSistema(role.getSistema().getId());
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
        }
        return authentication;
    }

    private boolean hasAuthority(String authority) {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> authority.equals(auth.getAuthority()));
    }
}

