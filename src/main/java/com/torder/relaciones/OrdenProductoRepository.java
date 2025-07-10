package com.torder.relaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenProductoRepository extends JpaRepository<OrdenProducto, Long> {
    
    // Find all ordenProductos for a specific orden
    List<OrdenProducto> findByOrdenId(Long ordenId);
    
    // Find all ordenProductos for a specific producto
    List<OrdenProducto> findByProductoId(Long productoId);
    
    // Find all ordenProductos for a specific orden and producto
    List<OrdenProducto> findByOrdenIdAndProductoId(Long ordenId, Long productoId);
} 