package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.VerificationCode;
import com.api.auth.Domain.Enum.TipoVerificacao;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    List<VerificationCode> findAllByTipoAndUsedFalseAndExpiryDateAfterOrderByExpiryDateDesc(TipoVerificacao tipo, Instant now);

    Optional<VerificationCode> findTopByUsuarioEmailAndTipoAndUsedFalseOrderByExpiryDateDesc(String email, TipoVerificacao tipo);

    Optional<VerificationCode> findByChallengeIdAndTipo(UUID challengeId, TipoVerificacao tipo);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode vc WHERE vc.usuario = :usuario AND vc.tipo = :tipo")
    void deleteByUsuarioAndTipo(@Param("usuario") Usuario usuario, @Param("tipo") TipoVerificacao tipo);
}
