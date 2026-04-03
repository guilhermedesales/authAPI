package com.api.auth.Application.Service;

import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Infra.Repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void revokeAllByUsuario(Usuario usuario) {
        int total = refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        log.info("[AUTH] Refresh tokens revoked - userId={} total={}", usuario.getId(), total);
    }
}
