package com.torder.mesa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Page<Mesa> findBySucursalId(Long sucursalId, Pageable pageable);
    Page<Mesa> findBySucursalNegocioId(Long negocioId, Pageable pageable);
}