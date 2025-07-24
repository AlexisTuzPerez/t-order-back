package com.torder.producto;

import java.util.List;
import java.util.Optional;

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
    
    // Método para buscar productos por ID de sucursal (sin paginación)
    @Query("SELECT DISTINCT p FROM Producto p JOIN p.sucursales ps WHERE ps.sucursal.id = :sucursalId AND ps.activo = true")
    List<Producto> findBySucursalId(@Param("sucursalId") Long sucursalId);
    
    // Método para buscar un producto por ID con sus sucursales
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.sucursales ps LEFT JOIN FETCH ps.sucursal WHERE p.id = :id")
    Optional<Producto> findByIdWithSucursales(@Param("id") Long id);
}