package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, UUID> {
    Optional<UsuarioSistema> findByUsuarioAndSistema(Usuario usuario, Sistema sistema);
}
