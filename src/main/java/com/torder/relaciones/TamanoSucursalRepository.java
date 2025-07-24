package com.torder.relaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamanoSucursalRepository extends JpaRepository<TamanoSucursal, Long> {
    
} 