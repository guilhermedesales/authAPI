package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.VerificationCode;
import com.api.auth.Domain.Enum.TipoVerificacao;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findByCode(String code);

    Optional<VerificationCode> findByCodeAndUsuarioEmailAndTipo(String code, String email, TipoVerificacao tipo);

    Optional<VerificationCode> findByChallengeIdAndCodeAndTipo(UUID challengeId, String code, TipoVerificacao tipo);

    Optional<VerificationCode> findByChallengeIdAndTipo(UUID challengeId, TipoVerificacao tipo);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode vc WHERE vc.usuario = :usuario AND vc.tipo = :tipo")
    void deleteByUsuarioAndTipo(@Param("usuario") Usuario usuario, @Param("tipo") TipoVerificacao tipo);
}
