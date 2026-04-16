package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUsuario(Usuario usuario);

    @Modifying(clearAutomatically = true)
    @Transactional
        @Query("""
        UPDATE RefreshToken rt
        SET rt.revoked = true
        WHERE rt.usuario.id = :usuarioId AND rt.revoked = false
    """)
    int revokeAllByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revoked = true
        WHERE rt.usuario.id = :usuarioId
          AND rt.revoked = false
          AND (rt.session IS NULL OR rt.session.id <> :sessionId)
    """)
    int revokeAllByUsuarioIdExceptSessionId(@Param("usuarioId") UUID usuarioId,
                                            @Param("sessionId") UUID sessionId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revoked = true
        WHERE rt.session.id = :sessionId AND rt.revoked = false
    """)
    int revokeAllBySessionId(@Param("sessionId") UUID sessionId);

    Optional<RefreshToken> findByTokenId(String tokenId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteAllExpired(@Param("now") Instant now);
}
