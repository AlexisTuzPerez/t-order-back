package com.torder.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.torder.mesa.MesaRepository;
import com.torder.relaciones.ModificadorSucursal;
import com.torder.relaciones.ModificadorSucursalRepository;
import com.torder.relaciones.ProductoSucursal;
import com.torder.relaciones.ProductoSucursalRepository;
import com.torder.relaciones.SubcategoriaSucursal;
import com.torder.relaciones.SubcategoriaSucursalRepository;
import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;
import com.torder.user.dto.UserMesaDTO;
import com.torder.user.dto.UserModificadorDTO;
import com.torder.user.dto.UserProductoDTO;
import com.torder.user.dto.UserSubcategoriaDTO;
import com.torder.user.dto.UserSucursalDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SucursalRepository sucursalRepository;
    private final MesaRepository mesaRepository;
    private final ProductoSucursalRepository productoSucursalRepository;
    private final ModificadorSucursalRepository modificadorSucursalRepository;
    private final SubcategoriaSucursalRepository subcategoriaSucursalRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    @GetMapping("/sucursales")
    public ResponseEntity<List<UserSucursalDTO>> getAllSucursales() {
        User currentUser = getCurrentUser();
        
        // Obtener solo las sucursales del negocio del usuario
        List<UserSucursalDTO> sucursales = sucursalRepository.findByNegocioId(currentUser.getNegocio().getId()).stream()
            .map(userService::convertToUserSucursalDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sucursales);
    }

    @GetMapping("/{sucursalId}/sucursal")
    public ResponseEntity<UserSucursalDTO> getSucursal(@PathVariable Long sucursalId) {
        User currentUser = getCurrentUser();
        
        return sucursalRepository.findById(sucursalId)
            .filter(sucursal -> sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId()))
            .map(userService::convertToUserSucursalDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{sucursalId}/mesas")
    public ResponseEntity<List<UserMesaDTO>> getMesasBySucursal(@PathVariable Long sucursalId) {
        User currentUser = getCurrentUser();
        
        // Verificar que la sucursal existe y pertenece al negocio del usuario
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
            .filter(s -> s.getNegocio().getId().equals(currentUser.getNegocio().getId()))
            .orElse(null);
            
        if (sucursal == null) {
            return ResponseEntity.notFound().build();
        }

        List<UserMesaDTO> mesas = mesaRepository.findBySucursalId(sucursalId, null).getContent().stream()
            .map(userService::convertToUserMesaDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(mesas);
    }

    @GetMapping("/{sucursalId}/productos")
    public ResponseEntity<List<UserProductoDTO>> getProductosBySucursal(@PathVariable Long sucursalId) {
        User currentUser = getCurrentUser();
        
        // Verificar que la sucursal existe y pertenece al negocio del usuario
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
            .filter(s -> s.getNegocio().getId().equals(currentUser.getNegocio().getId()))
            .orElse(null);
            
        if (sucursal == null) {
            return ResponseEntity.notFound().build();
        }

        List<ProductoSucursal> productoSucursales = productoSucursalRepository
            .findBySucursalIdAndActivoTrue(sucursalId);

        List<UserProductoDTO> productos = productoSucursales.stream()
            .map(ps -> userService.convertToUserProductoDTO(ps.getProducto()))
            .filter(p -> p.getPrecio() != null) // Solo productos con precio válido
            .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{sucursalId}/modificadores")
    public ResponseEntity<List<UserModificadorDTO>> getModificadoresBySucursal(@PathVariable Long sucursalId) {
        User currentUser = getCurrentUser();
        
        // Verificar que la sucursal existe y pertenece al negocio del usuario
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
            .filter(s -> s.getNegocio().getId().equals(currentUser.getNegocio().getId()))
            .orElse(null);
            
        if (sucursal == null) {
            return ResponseEntity.notFound().build();
        }

        List<ModificadorSucursal> modificadorSucursales = modificadorSucursalRepository
            .findBySucursalIdAndActivoTrue(sucursalId);

        List<UserModificadorDTO> modificadores = modificadorSucursales.stream()
            .map(ms -> userService.convertToUserModificadorDTO(ms.getModificador()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(modificadores);
    }

    @GetMapping("/{sucursalId}/subcategorias")
    public ResponseEntity<List<UserSubcategoriaDTO>> getSubcategoriasBySucursal(@PathVariable Long sucursalId) {
        User currentUser = getCurrentUser();
        
        // Verificar que la sucursal existe y pertenece al negocio del usuario
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
            .filter(s -> s.getNegocio().getId().equals(currentUser.getNegocio().getId()))
            .orElse(null);
            
        if (sucursal == null) {
            return ResponseEntity.notFound().build();
        }

        List<SubcategoriaSucursal> subcategoriaSucursales = subcategoriaSucursalRepository
            .findBySucursalIdAndActivoTrue(sucursalId);

        List<UserSubcategoriaDTO> subcategorias = subcategoriaSucursales.stream()
            .map(ss -> userService.convertToUserSubcategoriaDTO(ss.getSubcategoria()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(subcategorias);
    }
} 