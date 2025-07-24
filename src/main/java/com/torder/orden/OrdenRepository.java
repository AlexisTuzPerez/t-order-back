package com.torder.orden;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.torder.user.User;

@Repository
public interface OrdenRepository extends JpaRepository<com.torder.orden.Orden, Long> {
    List<Orden> findByEstado(Status estado);
    List<Orden> findByEstadoAndMesaSucursalId(Status estado, Long sucursalId);
    List<Orden> findByUsuarioOrderByFechaCreacionDesc(User usuario);
}
