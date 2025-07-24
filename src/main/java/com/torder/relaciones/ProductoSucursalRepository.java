package com.torder.relaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoSucursalRepository extends JpaRepository<ProductoSucursal, Long> {
    
    List<ProductoSucursal> findBySucursalIdAndActivoTrue(Long sucursalId);
} 