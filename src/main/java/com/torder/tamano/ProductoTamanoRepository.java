package com.torder.tamano;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.torder.relaciones.ProductoTamano;

@Repository
public interface ProductoTamanoRepository extends JpaRepository<ProductoTamano, Long> {
    boolean existsByProductoIdAndTamanoId(Long productoId, Long tamanoId);
    ProductoTamano findByProductoIdAndTamanoId(Long productoId, Long tamanoId);
    List<ProductoTamano> findByProductoId(Long productoId);
}
