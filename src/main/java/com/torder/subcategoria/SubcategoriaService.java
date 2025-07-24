package com.torder.subcategoria;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.relaciones.SubcategoriaSucursal;
import com.torder.relaciones.SubcategoriaSucursalRepository;
import com.torder.user.User;
import com.torder.user.UserRepository;

import jakarta.persistence.EntityManager;

@Service
@Transactional
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final UserRepository userRepository;
    private final SubcategoriaSucursalRepository subcategoriaSucursalRepository;
    private final EntityManager entityManager;

    @Autowired
    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository, 
                              UserRepository userRepository,
                              SubcategoriaSucursalRepository subcategoriaSucursalRepository,
                              EntityManager entityManager) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.userRepository = userRepository;
        this.subcategoriaSucursalRepository = subcategoriaSucursalRepository;
        this.entityManager = entityManager;
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

    public List<SubcategoriaDTO> obtenerSubcategoriasDeSucursal() {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        List<Subcategoria> subcategorias = subcategoriaRepository.findBySucursalId(currentUser.getSucursal().getId());
        return subcategorias.stream().map(this::convertirADTO).toList();
    }

    public SubcategoriaDTO obtenerPorId(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Subcategoria subcategoria = subcategoriaRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría no encontrada"));

        // Verificar que la subcategoría pertenece a la sucursal del usuario
        boolean perteneceASucursal = subcategoria.getSucursales().stream()
                .anyMatch(ss -> ss.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ss.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a esta subcategoría");
        }

        return convertirADTO(subcategoria);
    }

    public SubcategoriaDTO crear(Subcategoria subcategoria) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        // Convertir nombre a mayúsculas
        if (subcategoria.getNombre() != null) {
            subcategoria.setNombre(subcategoria.getNombre().trim().toUpperCase());
        }

        // Guardar la subcategoría
        Subcategoria subcategoriaGuardada = subcategoriaRepository.save(subcategoria);

        // Asignar automáticamente a la sucursal del usuario
        SubcategoriaSucursal nuevaRelacion = new SubcategoriaSucursal();
        nuevaRelacion.setSubcategoria(subcategoriaGuardada);
        nuevaRelacion.setSucursal(currentUser.getSucursal());
        nuevaRelacion.setActivo(true);

        subcategoriaSucursalRepository.save(nuevaRelacion);
        
        // Construir el DTO directamente sin recargar
        SubcategoriaDTO resultado = new SubcategoriaDTO();
        resultado.setId(subcategoriaGuardada.getId());
        resultado.setNombre(subcategoriaGuardada.getNombre());
        resultado.setSucursalesIds(List.of(currentUser.getSucursal().getId()));

        return resultado;
    }

    public SubcategoriaDTO actualizar(Long id, Subcategoria subcategoriaActualizada) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Subcategoria subcategoriaExistente = subcategoriaRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría no encontrada"));

        // Verificar que la subcategoría pertenece a la sucursal del usuario
        boolean perteneceASucursal = subcategoriaExistente.getSucursales().stream()
                .anyMatch(ss -> ss.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ss.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar esta subcategoría");
        }

        // Actualizar campos
        if (subcategoriaActualizada.getNombre() != null) {
            subcategoriaExistente.setNombre(subcategoriaActualizada.getNombre().trim().toUpperCase());
        }

        Subcategoria subcategoriaGuardada = subcategoriaRepository.save(subcategoriaExistente);
        
        // Recargar la subcategoría para incluir las relaciones
        Subcategoria subcategoriaConRelaciones = subcategoriaRepository.findByIdWithSucursales(subcategoriaGuardada.getId())
                .orElse(subcategoriaGuardada);
        
        return convertirADTO(subcategoriaConRelaciones);
    }

    public void eliminar(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Subcategoria subcategoria = subcategoriaRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría no encontrada"));

        // Verificar que la subcategoría pertenece a la sucursal del usuario
        boolean perteneceASucursal = subcategoria.getSucursales().stream()
                .anyMatch(ss -> ss.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ss.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar esta subcategoría");
        }

        // Eliminar físicamente las relaciones con sucursales
        subcategoria.getSucursales().stream()
                .filter(ss -> ss.getSucursal().getId().equals(currentUser.getSucursal().getId()))
                .forEach(ss -> subcategoriaSucursalRepository.delete(ss));

        // Eliminar físicamente la subcategoría
        subcategoriaRepository.delete(subcategoria);
    }

    private SubcategoriaDTO convertirADTO(Subcategoria subcategoria) {
        SubcategoriaDTO dto = new SubcategoriaDTO();
        dto.setId(subcategoria.getId());
        dto.setNombre(subcategoria.getNombre());
        
        // Obtener las sucursales activas de esta subcategoría
        List<Long> sucursalesIds = subcategoria.getSucursales().stream()
                .filter(ss -> ss.getActivo())
                .map(ss -> ss.getSucursal().getId())
                .toList();
        
        dto.setSucursalesIds(sucursalesIds);
        
        return dto;
    }
} 