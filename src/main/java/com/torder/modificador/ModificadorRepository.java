package com.torder.modificador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.torder.subcategoria.Subcategoria;

@Repository
public interface ModificadorRepository extends JpaRepository<Modificador, Long> {
    List<Modificador> findBySubcategoria(Subcategoria subcategoria);
    Page<Modificador> findBySubcategoria(Subcategoria subcategoria, Pageable pageable);
    boolean existsByNombreAndSubcategoria(String nombre, Subcategoria subcategoria);
    
    // Método para verificar si existe un modificador con el mismo nombre a nivel global
    boolean existsByNombre(String nombre);
    
    // Método para verificar si existe un modificador con el mismo nombre a nivel global (excluyendo el actual)
    boolean existsByNombreAndIdNot(String nombre, Long id);
    
    // Método para verificar si existe un modificador con el mismo nombre en la misma subcategoria (excluyendo el actual)
    boolean existsByNombreAndSubcategoriaAndIdNot(String nombre, Subcategoria subcategoria, Long id);
    
    // Método para buscar modificadores por subcategoría y sucursal usando la nueva estructura
    @Query("SELECT DISTINCT m FROM Modificador m JOIN m.subcategoria.sucursales ss WHERE ss.sucursal = :sucursal")
    Page<Modificador> findBySubcategoriaSucursal(@Param("sucursal") com.torder.sucursal.Sucursal sucursal, Pageable pageable);
    
    // Método para buscar modificadores por ID de sucursal
    @Query("SELECT DISTINCT m FROM Modificador m JOIN m.sucursales ms WHERE ms.sucursal.id = :sucursalId")
    Page<Modificador> findBySucursalesSucursalId(@Param("sucursalId") Long sucursalId, Pageable pageable);
    
    // Método para buscar modificadores por múltiples IDs de sucursal
    @Query("SELECT DISTINCT m FROM Modificador m JOIN m.sucursales ms WHERE ms.sucursal.id IN :sucursalIds")
    Page<Modificador> findBySucursalesSucursalIdIn(@Param("sucursalIds") List<Long> sucursalIds, Pageable pageable);
    
    // Método para buscar modificadores por ID de sucursal (sin paginación)
    @Query("SELECT DISTINCT m FROM Modificador m JOIN m.sucursales ms WHERE ms.sucursal.id = :sucursalId AND ms.activo = true")
    List<Modificador> findBySucursalId(@Param("sucursalId") Long sucursalId);
} 