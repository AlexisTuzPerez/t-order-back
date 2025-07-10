package com.torder.relaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenProductoModificadorRepository extends JpaRepository<OrdenProductoModificador, Long> {
    
    // Find all modificadores for a specific ordenProducto
    List<OrdenProductoModificador> findByOrdenProductoId(Long ordenProductoId);
    
    // Find all modificadores for a specific orden
    List<OrdenProductoModificador> findByOrdenProducto_OrdenId(Long ordenId);
    
    // Delete all modificadores for a specific ordenProducto
    void deleteByOrdenProductoId(Long ordenProductoId);
} 