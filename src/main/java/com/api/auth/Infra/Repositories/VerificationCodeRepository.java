package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.VerificationCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findByCode(String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode vc WHERE vc.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
}
