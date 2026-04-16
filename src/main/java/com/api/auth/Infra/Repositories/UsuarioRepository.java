package com.api.auth.Infra.Repositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.auth.Domain.Entities.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>{
    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    Page<Usuario> findAll(Pageable pageable);

    @Query(
            value = "SELECT u FROM Usuario u JOIN UsuarioSistema us ON us.usuario = u WHERE us.sistema.id = :sistemaId",
            countQuery = "SELECT COUNT(u) FROM Usuario u JOIN UsuarioSistema us ON us.usuario = u WHERE us.sistema.id = :sistemaId"
    )
    Page<Usuario> findBySistemaId(@Param("sistemaId") UUID sistemaId, Pageable pageable);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u JOIN UsuarioSistema us ON us.usuario = u WHERE u.id = :usuarioId AND us.sistema.id = :sistemaId")
    boolean existsByIdAndSistemaId(@Param("usuarioId") UUID usuarioId, @Param("sistemaId") UUID sistemaId);

    Optional<Usuario> findById(UUID id);
}
