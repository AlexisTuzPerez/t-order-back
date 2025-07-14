package com.torder.modificador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    
    // Método para buscar modificadores por subcategoría y sucursal
    Page<Modificador> findBySubcategoriaSucursal(com.torder.sucursal.Sucursal sucursal, Pageable pageable);
} 