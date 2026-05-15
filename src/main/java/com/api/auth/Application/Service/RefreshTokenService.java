package com.api.auth.Application.Service;

import com.api.auth.Domain.Entities.UserSession;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Infra.Repositories.RefreshTokenRepository;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserSessionRepository userSessionRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void revokeAllByUsuario(Usuario usuario) {
        Instant now = Instant.now();
        int revokedTokens = refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        int revokedSessions = userSessionRepository.revokeAllByUsuarioId(usuario.getId(), now);
        log.info("[AUTH] User security revoke all executed - userId={} revokedTokens={} revokedSessions={}",
                usuario.getId(), revokedTokens, revokedSessions);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void revokeAllByUsuarioExceptOne(Usuario usuario, UUID userSession) {
        Instant now = Instant.now();
        int revokedTokens = refreshTokenRepository.revokeAllByUsuarioIdExceptSessionId(usuario.getId(), userSession);
        int revokedSessions = userSessionRepository.revokeAllByUsuarioIdExceptSession(usuario.getId(), userSession, now);
        log.info("[AUTH] User security revoke all except one executed - userId={} revokedTokens={} revokedSessions={}",
                usuario.getId(), revokedTokens, revokedSessions);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void revokeBySession(UserSession session) {
        Instant now = Instant.now();
        int revokedTokens = refreshTokenRepository.revokeAllBySessionId(session.getId());
        int revokedSessions = userSessionRepository.revokeBySessionId(session.getId(), now);
        log.warn("[AUTH] Session security revoke executed - sessionId={} revokedTokens={} revokedSessions={}",
                session.getId(), revokedTokens, revokedSessions);
    }

}
