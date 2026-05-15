package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, UUID> {
    Optional<UsuarioSistema> findByUsuarioAndSistema(Usuario usuario, Sistema sistema);

    @Query("""
            SELECT DISTINCT us
            FROM UsuarioSistema us
            JOIN FETCH us.role r
            LEFT JOIN FETCH r.permissoes
            WHERE us.usuario = :usuario AND us.sistema = :sistema
            """)
    Optional<UsuarioSistema> findByUsuarioAndSistemaWithRolePermissoes(@Param("usuario") Usuario usuario,
                                                                        @Param("sistema") Sistema sistema);

    Page<UsuarioSistema> findByUsuarioId(UUID usuarioId, Pageable pageable);
    Page<UsuarioSistema> findBySistemaId(UUID sistemaId, Pageable pageable);
    Page<UsuarioSistema> findByUsuarioIdAndSistemaId(UUID usuarioId, UUID sistemaId,  Pageable pageable);

    boolean existsByUsuarioIdAndSistemaId(UUID usuarioId, UUID sistemaId);

    Optional<UsuarioSistema> findByUsuario(Usuario usuario);
}
