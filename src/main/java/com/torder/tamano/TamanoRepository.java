package com.torder.tamano;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.torder.negocioCliente.NegocioCliente;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Long> {
    List<Tamano> findByNegocio(NegocioCliente negocio);
    Page<Tamano> findByNegocio(NegocioCliente negocio, Pageable pageable);
    boolean existsByNombreAndNegocio(String nombre, NegocioCliente negocio);
    
    // Método para verificar si existe un tamaño con el mismo nombre en el mismo negocio (excluyendo el actual)
    boolean existsByNombreAndNegocioAndIdNot(String nombre, NegocioCliente negocio, Long id);
}
