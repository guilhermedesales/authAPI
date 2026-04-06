package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.UserSession;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

	Optional<UserSession> findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(UUID usuarioId, UUID sistemaId, UUID deviceId);

	boolean existsByUsuarioIdAndSistemaIdAndRevokedAtIsNull(UUID usuarioId, UUID sistemaId);

	boolean existsByUsuarioIdAndSistemaIdAndIpAndRevokedAtIsNull(UUID usuarioId, UUID sistemaId, String ip);

	@Query("""
		SELECT DISTINCT us.location
		FROM UserSession us
		WHERE us.usuario.id = :usuarioId
		  AND us.sistema.id = :sistemaId
		  AND us.revokedAt IS NULL
		  AND us.location IS NOT NULL
		  AND us.location <> ''
	""")
	List<String> findDistinctActiveLocations(@Param("usuarioId") UUID usuarioId, @Param("sistemaId") UUID sistemaId);

    Page<UserSession> findAllByUsuarioId(UUID usuarioId, Pageable pageable);
}
