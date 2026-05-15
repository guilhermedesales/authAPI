package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Sistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SistemaRepository extends JpaRepository<Sistema, UUID> {

    Page<Sistema> findAll(Pageable pageable);

    @Query("SELECT s FROM Sistema s LEFT JOIN FETCH s.roles WHERE s.id = :id")
    Optional<Sistema> findByIdWithRoles(UUID id);

    Optional<Sistema> findByNomeIgnoreCase(String nome);
}
