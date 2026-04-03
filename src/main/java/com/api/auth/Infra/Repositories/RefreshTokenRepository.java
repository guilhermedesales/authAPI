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

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.usuario = :usuario AND rt.revoked = false")
    int revokeAllByUsuario(@Param("usuario") Usuario usuario);

    Optional<RefreshToken> findByTokenId(String tokenId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteAllExpired(@Param("now") Instant now);
}
