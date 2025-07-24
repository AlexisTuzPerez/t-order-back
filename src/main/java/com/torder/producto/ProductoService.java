package com.torder.producto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.relaciones.ProductoSucursal;
import com.torder.relaciones.ProductoSucursalRepository;
import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final UserRepository userRepository;
    private final ProductoSucursalRepository productoSucursalRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, 
                          UserRepository userRepository,
                          ProductoSucursalRepository productoSucursalRepository,
                          SubcategoriaRepository subcategoriaRepository) {
        this.productoRepository = productoRepository;
        this.userRepository = userRepository;
        this.productoSucursalRepository = productoSucursalRepository;
        this.subcategoriaRepository = subcategoriaRepository;
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

    public List<ProductoDTO> obtenerProductosDeSucursal() {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        List<Producto> productos = productoRepository.findBySucursalId(currentUser.getSucursal().getId());
        return productos.stream().map(this::convertirADTO).toList();
    }

    public ProductoDTO obtenerPorId(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Producto producto = productoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Verificar que el producto pertenece a la sucursal del usuario
        boolean perteneceASucursal = producto.getSucursales().stream()
                .anyMatch(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ps.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este producto");
        }

        return convertirADTO(producto);
    }

    public ProductoDTO crear(ProductoDTO productoDTO) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        // Validar que la subcategoría sea obligatoria
        if (productoDTO.getSubcategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La subcategoría es obligatoria");
        }

        // Crear el producto
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setImagenUrl(productoDTO.getImagenUrl());
        producto.setActivo(productoDTO.getActivo() != null ? productoDTO.getActivo() : true);

        // Asignar la subcategoría
        Subcategoria subcategoria = subcategoriaRepository.findById(productoDTO.getSubcategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría no encontrada"));
        producto.setSubcategoria(subcategoria);

        // Guardar el producto
        Producto productoGuardado = productoRepository.save(producto);

        // Asignar automáticamente a la sucursal del usuario
        ProductoSucursal nuevaRelacion = new ProductoSucursal();
        nuevaRelacion.setProducto(productoGuardado);
        nuevaRelacion.setSucursal(currentUser.getSucursal());
        nuevaRelacion.setActivo(true);

        productoSucursalRepository.save(nuevaRelacion);
        
        // Construir el DTO directamente sin recargar
        ProductoDTO resultado = new ProductoDTO();
        resultado.setId(productoGuardado.getId());
        resultado.setNombre(productoGuardado.getNombre());
        resultado.setPrecio(productoGuardado.getPrecio());
        resultado.setImagenUrl(productoGuardado.getImagenUrl());
        resultado.setActivo(productoGuardado.getActivo());
        resultado.setSubcategoriaId(subcategoria.getId());
        resultado.setSubcategoriaNombre(subcategoria.getNombre());
        resultado.setSucursalesIds(List.of(currentUser.getSucursal().getId()));

        return resultado;
    }

    public ProductoDTO actualizar(Long id, Producto productoActualizado) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Producto productoExistente = productoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Verificar que el producto pertenece a la sucursal del usuario
        boolean perteneceASucursal = productoExistente.getSucursales().stream()
                .anyMatch(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ps.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este producto");
        }

        // Actualizar campos
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setPrecio(productoActualizado.getPrecio());

        Producto productoGuardado = productoRepository.save(productoExistente);
        
        // Recargar el producto para incluir las relaciones
        Producto productoConRelaciones = productoRepository.findByIdWithSucursales(productoGuardado.getId())
                .orElse(productoGuardado);
        
        return convertirADTO(productoConRelaciones);
    }

    public void eliminar(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Producto producto = productoRepository.findByIdWithSucursales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Verificar que el producto pertenece a la sucursal del usuario
        boolean perteneceASucursal = producto.getSucursales().stream()
                .anyMatch(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ps.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este producto");
        }

        // Eliminar físicamente las relaciones con sucursales
        producto.getSucursales().stream()
                .filter(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()))
                .forEach(ps -> productoSucursalRepository.delete(ps));

        // Eliminar físicamente el producto
        productoRepository.delete(producto);
    }

    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());
        
        // Asignar información de subcategoría
        if (producto.getSubcategoria() != null) {
            dto.setSubcategoriaId(producto.getSubcategoria().getId());
            dto.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }
        
        // Obtener las sucursales activas de este producto
        List<Long> sucursalesIds = producto.getSucursales().stream()
                .filter(ps -> ps.getActivo())
                .map(ps -> ps.getSucursal().getId())
                .toList();
        
        dto.setSucursalesIds(sucursalesIds);
        
        return dto;
    }
} 