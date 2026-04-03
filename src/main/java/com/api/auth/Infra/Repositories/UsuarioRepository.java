package com.api.auth.Infra.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.auth.Domain.Entities.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>{
    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);
}
