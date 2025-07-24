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
import com.torder.relaciones.ProductoTamano;
import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.tamano.ProductoTamanoRepository;
import com.torder.tamano.Tamano;
import com.torder.tamano.TamanoRepository;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final UserRepository userRepository;
    private final ProductoSucursalRepository productoSucursalRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ProductoTamanoRepository productoTamanoRepository;
    private final TamanoRepository tamanoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, 
                          UserRepository userRepository,
                          ProductoSucursalRepository productoSucursalRepository,
                          SubcategoriaRepository subcategoriaRepository,
                          ProductoTamanoRepository productoTamanoRepository,
                          TamanoRepository tamanoRepository) {
        this.productoRepository = productoRepository;
        this.userRepository = userRepository;
        this.productoSucursalRepository = productoSucursalRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.productoTamanoRepository = productoTamanoRepository;
        this.tamanoRepository = tamanoRepository;
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

    public ProductoDTO crear(ProductoCreacionDTO productoCreacionDTO) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        // Validar que la subcategoría sea obligatoria
        if (productoCreacionDTO.getSubcategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La subcategoría es obligatoria");
        }

        // Crear el producto
        Producto producto = new Producto();
        // Convertir nombre a mayúsculas
        if (productoCreacionDTO.getNombre() != null) {
            producto.setNombre(productoCreacionDTO.getNombre().trim().toUpperCase());
        } else {
            producto.setNombre(productoCreacionDTO.getNombre());
        }
        producto.setPrecio(productoCreacionDTO.getPrecio());
        producto.setImagenUrl(productoCreacionDTO.getImagenUrl());
        producto.setActivo(productoCreacionDTO.getActivo() != null ? productoCreacionDTO.getActivo() : true);

        // Asignar la subcategoría
        Subcategoria subcategoria = subcategoriaRepository.findById(productoCreacionDTO.getSubcategoriaId())
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
        
        // Procesar tamaños si se proporcionan
        if (productoCreacionDTO.getTamaños() != null && !productoCreacionDTO.getTamaños().isEmpty()) {
            for (ProductoCreacionDTO.TamanoCreacion tamanoCreacion : productoCreacionDTO.getTamaños()) {
                if (tamanoCreacion.getTamanoId() != null && tamanoCreacion.getPrecio() != null) {
                    Tamano tamano = tamanoRepository.findById(tamanoCreacion.getTamanoId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Tamaño con ID " + tamanoCreacion.getTamanoId() + " no encontrado"));
                    
                    ProductoTamano productoTamano = new ProductoTamano();
                    productoTamano.setProducto(productoGuardado);
                    productoTamano.setTamano(tamano);
                    productoTamano.setPrecio(tamanoCreacion.getPrecio());
                    
                    productoTamanoRepository.save(productoTamano);
                }
            }
        }
        
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

        // Incluir los tamaños en la respuesta
        if (productoCreacionDTO.getTamaños() != null && !productoCreacionDTO.getTamaños().isEmpty()) {
            List<ProductoDTO.TamanoInfo> tamanosInfo = productoCreacionDTO.getTamaños().stream()
                    .map(tc -> {
                        Tamano tamano = tamanoRepository.findById(tc.getTamanoId()).orElse(null);
                        if (tamano != null) {
                            return new ProductoDTO.TamanoInfo(
                                    tamano.getId(),
                                    tamano.getNombre(),
                                    tamano.getDescripcion(),
                                    tc.getPrecio()
                            );
                        }
                        return null;
                    })
                    .filter(ti -> ti != null)
                    .toList();
            resultado.setTamaños(tamanosInfo);
        }

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
        if (productoActualizado.getNombre() != null) {
            productoExistente.setNombre(productoActualizado.getNombre().trim().toUpperCase());
        }
        productoExistente.setPrecio(productoActualizado.getPrecio());

        Producto productoGuardado = productoRepository.save(productoExistente);
        
        // Recargar el producto para incluir las relaciones
        Producto productoConRelaciones = productoRepository.findByIdWithSucursales(productoGuardado.getId())
                .orElse(productoGuardado);
        
        return convertirADTO(productoConRelaciones);
    }

    public ProductoDTO actualizarTamanos(Long productoId, List<ProductoCreacionDTO.TamanoCreacion> tamanosCreacion) {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        Producto producto = productoRepository.findByIdWithSucursales(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Verificar que el producto pertenece a la sucursal del usuario
        boolean perteneceASucursal = producto.getSucursales().stream()
                .anyMatch(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()) && ps.getActivo());

        if (!perteneceASucursal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este producto");
        }

        // Eliminar tamaños existentes
        List<ProductoTamano> tamanosExistentes = productoTamanoRepository.findByProductoId(productoId);
        productoTamanoRepository.deleteAll(tamanosExistentes);

        // Agregar nuevos tamaños
        if (tamanosCreacion != null && !tamanosCreacion.isEmpty()) {
            for (ProductoCreacionDTO.TamanoCreacion tamanoCreacion : tamanosCreacion) {
                if (tamanoCreacion.getTamanoId() != null && tamanoCreacion.getPrecio() != null) {
                    Tamano tamano = tamanoRepository.findById(tamanoCreacion.getTamanoId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Tamaño con ID " + tamanoCreacion.getTamanoId() + " no encontrado"));
                    
                    ProductoTamano productoTamano = new ProductoTamano();
                    productoTamano.setProducto(producto);
                    productoTamano.setTamano(tamano);
                    productoTamano.setPrecio(tamanoCreacion.getPrecio());
                    
                    productoTamanoRepository.save(productoTamano);
                }
            }
        }

        // Recargar el producto para incluir las relaciones
        Producto productoConRelaciones = productoRepository.findByIdWithSucursales(productoId)
                .orElse(producto);
        
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
        
        // Obtener los tamaños de este producto
        List<ProductoTamano> productoTamanos = productoTamanoRepository.findByProductoId(producto.getId());
        List<ProductoDTO.TamanoInfo> tamanosInfo = productoTamanos.stream()
                .map(pt -> new ProductoDTO.TamanoInfo(
                        pt.getTamano().getId(),
                        pt.getTamano().getNombre(),
                        pt.getTamano().getDescripcion(),
                        pt.getPrecio()
                ))
                .toList();
        
        dto.setTamaños(tamanosInfo);
        
        return dto;
    }
} 