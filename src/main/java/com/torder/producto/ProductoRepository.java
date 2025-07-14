package com.torder.producto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN p.sucursales ps WHERE ps.sucursal.id = :sucursalId")
    Page<Producto> findBySucursalesSucursalId(@Param("sucursalId") Long sucursalId, Pageable pageable);
    
    Page<Producto> findByNegocioId(Long negocioId, Pageable pageable);
}