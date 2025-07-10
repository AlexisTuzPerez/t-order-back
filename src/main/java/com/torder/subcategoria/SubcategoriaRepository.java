package com.torder.subcategoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
    Page<Subcategoria> findBySucursalId(Long sucursalId, Pageable pageable);
    Page<Subcategoria> findBySucursalNegocioId(Long negocioId, Pageable pageable);
}