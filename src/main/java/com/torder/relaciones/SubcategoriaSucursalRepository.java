package com.torder.relaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubcategoriaSucursalRepository extends JpaRepository<SubcategoriaSucursal, Long> {
    
} 