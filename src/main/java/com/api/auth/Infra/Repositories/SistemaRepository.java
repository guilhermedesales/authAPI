package com.api.auth.Infra.Repositories;

import com.api.auth.Domain.Entities.Sistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SistemaRepository extends JpaRepository<Sistema, UUID> {

    Page<Sistema> findAll(Pageable pageable);

}
