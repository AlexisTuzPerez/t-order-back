package com.torder.relaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModificadorSucursalRepository extends JpaRepository<ModificadorSucursal, Long> {
    
    List<ModificadorSucursal> findBySucursalIdAndActivoTrue(Long sucursalId);
} 