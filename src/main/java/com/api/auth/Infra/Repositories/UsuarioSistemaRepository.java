package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, UUID> {
}
