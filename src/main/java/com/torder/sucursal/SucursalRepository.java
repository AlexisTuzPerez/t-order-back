package com.torder.sucursal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    
    List<Sucursal> findByNegocioId(Long negocioId);
    
}
