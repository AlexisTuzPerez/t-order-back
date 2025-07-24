package com.torder.subcategoria;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
    
    @Query("SELECT s FROM Subcategoria s JOIN s.sucursales ss WHERE ss.sucursal.id = :sucursalId AND ss.activo = true")
    List<Subcategoria> findBySucursalId(@Param("sucursalId") Long sucursalId);
    
    @Query("SELECT s FROM Subcategoria s LEFT JOIN FETCH s.sucursales ss LEFT JOIN FETCH ss.sucursal WHERE s.id = :id")
    Optional<Subcategoria> findByIdWithSucursales(@Param("id") Long id);
} 