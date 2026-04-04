package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.UserSession;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("""
		UPDATE UserSession us
		SET us.revokedAt = :now
		WHERE us.id = :sessionId AND us.revokedAt IS NULL
	""")
	int revokeBySessionId(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("""
		UPDATE UserSession us
		SET us.revokedAt = :now
		WHERE us.usuario.id = :usuarioId AND us.revokedAt IS NULL
	""")
	int revokeAllByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("now") Instant now);
}
