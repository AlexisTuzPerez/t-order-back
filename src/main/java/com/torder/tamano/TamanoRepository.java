package com.torder.tamano;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Long> {
    // Métodos eliminados:
    // List<Tamano> findByNegocio(NegocioCliente negocio);
    // Page<Tamano> findByNegocio(NegocioCliente negocio, Pageable pageable);
    // boolean existsByNombreAndNegocio(String nombre, NegocioCliente negocio);
    // boolean existsByNombreAndNegocioAndIdNot(String nombre, NegocioCliente negocio, Long id);
    
    // Método para buscar tamaños por ID de sucursal (sin paginación)
    @Query("SELECT DISTINCT t FROM Tamano t JOIN t.sucursales ts WHERE ts.sucursal.id = :sucursalId AND ts.activo = true")
    List<Tamano> findBySucursalId(@Param("sucursalId") Long sucursalId);
    
    // Método para buscar un tamaño por ID con sus sucursales
    @Query("SELECT t FROM Tamano t LEFT JOIN FETCH t.sucursales ts LEFT JOIN FETCH ts.sucursal WHERE t.id = :id")
    Optional<Tamano> findByIdWithSucursales(@Param("id") Long id);
    
    // Métodos de paginación por sucursal
    @Query("SELECT DISTINCT t FROM Tamano t JOIN t.sucursales ts WHERE ts.sucursal.id = :sucursalId")
    Page<Tamano> findBySucursalesSucursalId(@Param("sucursalId") Long sucursalId, Pageable pageable);
    
    @Query("SELECT DISTINCT t FROM Tamano t JOIN t.sucursales ts WHERE ts.sucursal.id IN :sucursalIds")
    Page<Tamano> findBySucursalesSucursalIdIn(@Param("sucursalIds") List<Long> sucursalIds, Pageable pageable);
}
