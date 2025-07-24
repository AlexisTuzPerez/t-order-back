package com.torder.tamano;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.relaciones.TamanoSucursal;
import com.torder.relaciones.TamanoSucursalRepository;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class TamanoService {

    private final TamanoRepository tamanoRepository;
    private final UserRepository userRepository;
    private final TamanoSucursalRepository tamanoSucursalRepository;

    @Autowired
    public TamanoService(TamanoRepository tamanoRepository, 
                         UserRepository userRepository,
                         TamanoSucursalRepository tamanoSucursalRepository) {
        this.tamanoRepository = tamanoRepository;
        this.userRepository = userRepository;
        this.tamanoSucursalRepository = tamanoSucursalRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private void validateUserAccess(Long sucursalId) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null || !currentUser.getSucursal().getId().equals(sucursalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a esta sucursal");
        }
    }

    public List<TamanoDTO> obtenerTamanosDeSucursal() {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        List<Tamano> tamanos = tamanoRepository.findBySucursalId(currentUser.getSucursal().getId());
        return tamanos.stream().map(this::convertirADTO).toList();
    }

    public TamanoDTO obtenerPorId(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Tamano tamano = tamanoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño no encontrado"));

        // Verificar que el tamaño pertenece a la sucursal del usuario
        boolean perteneceASucursal = tamano.getSucursales().stream()
                .anyMatch(ts -> ts.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ts.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este tamaño");
        }

        return convertirADTO(tamano);
    }

    public TamanoDTO crear(Tamano tamano) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        // Guardar el tamaño
        Tamano tamanoGuardado = tamanoRepository.save(tamano);

        // Asignar automáticamente a la sucursal del usuario
        TamanoSucursal nuevaRelacion = new TamanoSucursal();
        nuevaRelacion.setTamano(tamanoGuardado);
        nuevaRelacion.setSucursal(currentUser.getSucursal());
        nuevaRelacion.setActivo(true);

        tamanoSucursalRepository.save(nuevaRelacion);
        
        // Construir el DTO directamente sin recargar
        TamanoDTO resultado = new TamanoDTO();
        resultado.setId(tamanoGuardado.getId());
        resultado.setNombre(tamanoGuardado.getNombre());
        resultado.setDescripcion(tamanoGuardado.getDescripcion());
        resultado.setSucursalesIds(List.of(currentUser.getSucursal().getId()));

        return resultado;
    }

    public TamanoDTO actualizar(Long id, Tamano tamanoActualizado) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Tamano tamanoExistente = tamanoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño no encontrado"));

        // Verificar que el tamaño pertenece a la sucursal del usuario
        boolean perteneceASucursal = tamanoExistente.getSucursales().stream()
                .anyMatch(ts -> ts.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ts.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este tamaño");
        }

        // Actualizar campos
        tamanoExistente.setNombre(tamanoActualizado.getNombre());

        Tamano tamanoGuardado = tamanoRepository.save(tamanoExistente);
        
        // Recargar el tamaño para incluir las relaciones
        Tamano tamanoConRelaciones = tamanoRepository.findByIdWithSucursales(tamanoGuardado.getId())
                .orElse(tamanoGuardado);
        
        return convertirADTO(tamanoConRelaciones);
    }

    public void eliminar(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Tamano tamano = tamanoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño no encontrado"));

        // Verificar que el tamaño pertenece a la sucursal del usuario
        boolean perteneceASucursal = tamano.getSucursales().stream()
                .anyMatch(ts -> ts.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ts.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este tamaño");
        }

        // Eliminar físicamente las relaciones con sucursales
        tamano.getSucursales().stream()
                .filter(ts -> ts.getSucursal().getId().equals(currentUser.getSucursal().getId()))
                .forEach(ts -> tamanoSucursalRepository.delete(ts));

        // Eliminar físicamente el tamaño
        tamanoRepository.delete(tamano);
    }

    private TamanoDTO convertirADTO(Tamano tamano) {
        TamanoDTO dto = new TamanoDTO();
        dto.setId(tamano.getId());
        dto.setNombre(tamano.getNombre());
        dto.setDescripcion(tamano.getDescripcion());
        // Obtener las sucursales activas de este tamaño
        List<Long> sucursalesIds = tamano.getSucursales().stream()
                .filter(ts -> ts.getActivo())
                .map(ts -> ts.getSucursal().getId())
                .toList();
        dto.setSucursalesIds(sucursalesIds);
        return dto;
    }
} 