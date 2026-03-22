package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Permissao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {
    Page<Permissao> findAll (Pageable pageable);
}
