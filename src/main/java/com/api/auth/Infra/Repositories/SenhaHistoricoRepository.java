package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.SenhaHistorico;
import com.api.auth.Domain.Entities.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SenhaHistoricoRepository extends JpaRepository<SenhaHistorico, UUID> {

    @Query("SELECT sh FROM SenhaHistorico sh WHERE sh.usuario = :usuario ORDER BY sh.createdAt DESC")
    List<SenhaHistorico> findUltimasSenhas(@Param("usuario") Usuario usuario, Pageable pageable);
}