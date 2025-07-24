package com.torder.relaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubcategoriaSucursalRepository extends JpaRepository<SubcategoriaSucursal, Long> {
    
    List<SubcategoriaSucursal> findBySucursalIdAndActivoTrue(Long sucursalId);
} 