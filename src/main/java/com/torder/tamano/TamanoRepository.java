package com.torder.tamano;

import com.torder.negocioCliente.NegocioCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Long> {
    List<Tamano> findByNegocio(NegocioCliente negocio);
    boolean existsByNombreAndNegocio(String nombre, NegocioCliente negocio);
}
