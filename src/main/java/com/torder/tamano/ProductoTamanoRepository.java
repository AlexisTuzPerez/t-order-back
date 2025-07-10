package com.torder.tamano;

import com.torder.relaciones.ProductoTamano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoTamanoRepository extends JpaRepository<ProductoTamano, Long> {
    boolean existsByProductoIdAndTamanoId(Long productoId, Long tamanoId);
    ProductoTamano findByProductoIdAndTamanoId(Long productoId, Long tamanoId);
}
