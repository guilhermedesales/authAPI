package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Page<Role> findAll(Pageable pageable);

    Page<Role> findBySistemaId(UUID sistemaId, Pageable pageable);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissoes WHERE r.id = :id")
    Optional<Role> findByIdWithRoles(UUID id);

    Optional<Role> findByIdAndSistemaId(UUID usuarioId, UUID sistemaId);

    Optional<Role> findByNomeAndSistemaId(String nome, UUID sistemaId);

    Optional<Role> findByNomeAndSistemaIsNull(String nome);

    List<Role> findBySistemaId(UUID sistemaId);
}
