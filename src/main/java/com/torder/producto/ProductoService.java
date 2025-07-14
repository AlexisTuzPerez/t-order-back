package com.torder.producto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.negocioCliente.NegocioCliente;
import com.torder.negocioCliente.NegocioClienteRepository;
import com.torder.relaciones.ProductoSucursal;
import com.torder.relaciones.ProductoTamano;
import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;
import com.torder.tamano.ProductoTamanoRepository;
import com.torder.tamano.Tamano;
import com.torder.tamano.TamanoRepository;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final NegocioClienteRepository negocioClienteRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final UserRepository userRepository;
    private final TamanoRepository tamanoRepository;
    private final ProductoTamanoRepository productoTamanoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, 
                          SucursalRepository sucursalRepository,
                          NegocioClienteRepository negocioClienteRepository,
                          SubcategoriaRepository subcategoriaRepository,
                          UserRepository userRepository,
                          TamanoRepository tamanoRepository,
                          ProductoTamanoRepository productoTamanoRepository) {
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
        this.negocioClienteRepository = negocioClienteRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.userRepository = userRepository;
        this.tamanoRepository = tamanoRepository;
        this.productoTamanoRepository = productoTamanoRepository;
    }

    public Page<ProductoDTO> getAllProductos(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Producto> productos = productoRepository.findAll(pageable);
            return productos.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.SUCURSAL) {
            Page<Producto> productos = productoRepository.findBySucursalesSucursalId(currentUser.getSucursal().getId(), pageable);
            return productos.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.OWNER) {
            Page<Producto> productos = productoRepository.findByNegocioId(currentUser.getNegocio().getId(), pageable);
            return productos.map(this::convertToDTO);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    }

    public ProductoDTO getProductoById(Long id) {
        User currentUser = getCurrentUser();
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto not found with id: " + id));

        tieneAccesoProducto(currentUser, producto);
        return convertToDTO(producto);
    }

    public ProductoDTO createProducto(ProductoDTO productoDTO) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.SUCURSAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden crear productos.");
        }
        
        // Validaciones básicas
        if (productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio.");
        }
        
        if (productoDTO.getPrecio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto es obligatorio.");
        }
        if (productoDTO.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto debe ser mayor o igual a 0.");
        }
        
        // Determinar el negocio del producto
        NegocioCliente negocio;
        if (currentUser.getRole() == Role.ADMIN) {
            if (productoDTO.getNegocioId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El negocio es obligatorio para crear un producto.");
            }
            negocio = negocioClienteRepository.findById(productoDTO.getNegocioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negocio not found with id: " + productoDTO.getNegocioId()));
        } else {
            negocio = currentUser.getNegocio();
        }
        
        // Crear el producto
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre().trim());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setImagenUrl(productoDTO.getImagenUrl());
        producto.setActivo(productoDTO.getActivo() != null ? productoDTO.getActivo() : true);
        producto.setNegocio(negocio);
        
        // Validar que la subcategoría sea obligatoria
        if (productoDTO.getSubcategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La subcategoría es obligatoria.");
        }
        
        // Asignar subcategoría
        Subcategoria subcategoria = subcategoriaRepository.findById(productoDTO.getSubcategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría not found with id: " + productoDTO.getSubcategoriaId()));
        producto.setSubcategoria(subcategoria);
        
        // Guardar el producto primero
        Producto savedProducto = productoRepository.save(producto);
        
        // Manejar la asignación a sucursales
        if (productoDTO.getSucursales() != null && !productoDTO.getSucursales().isEmpty()) {
            // Asignar a sucursales específicas
            Set<Long> sucursalesIds = new HashSet<>();
            for (ProductoDTO.SucursalInfo sucursalInfo : productoDTO.getSucursales()) {
                // Verificar que no hay duplicados
                if (!sucursalesIds.add(sucursalInfo.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal duplicada: " + sucursalInfo.getId());
                }
                
                Sucursal sucursal = sucursalRepository.findById(sucursalInfo.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + sucursalInfo.getId()));
                
                // Verificar que la sucursal pertenece al negocio del usuario
                if (currentUser.getRole() != Role.ADMIN && !sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar productos a sucursales de otros negocios.");
                }
                
                ProductoSucursal productoSucursal = new ProductoSucursal();
                productoSucursal.setProducto(savedProducto);
                productoSucursal.setSucursal(sucursal);
                productoSucursal.setActivo(true);
                // No usar setProducto() para evitar duplicados en la relación bidireccional
                savedProducto.getSucursales().add(productoSucursal);
            }
        } else {
            // Asignar a todas las sucursales del negocio
            List<Sucursal> sucursalesNegocio = sucursalRepository.findByNegocioId(negocio.getId());
            for (Sucursal sucursal : sucursalesNegocio) {
                ProductoSucursal productoSucursal = new ProductoSucursal();
                productoSucursal.setProducto(savedProducto);
                productoSucursal.setSucursal(sucursal);
                productoSucursal.setActivo(true);
                // No usar setProducto() para evitar duplicados en la relación bidireccional
                savedProducto.getSucursales().add(productoSucursal);
            }
        }
        
        // Manejar la asignación de tamaños (opcional) - DESPUÉS de guardar el producto
        if (productoDTO.getTamanos() != null && !productoDTO.getTamanos().isEmpty()) {
            Set<Long> tamanosIds = new HashSet<>();
            Double precioMasBajo = Double.MAX_VALUE;
            
            for (ProductoDTO.TamanoInfo tamanoInfo : productoDTO.getTamanos()) {
                // Validar que se proporciona el ID del tamaño
                if (tamanoInfo.getId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del tamaño es obligatorio.");
                }
                
                // Verificar que no hay duplicados
                if (!tamanosIds.add(tamanoInfo.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tamaño duplicado: " + tamanoInfo.getId());
                }
                
                // Validar precio del tamaño
                if (tamanoInfo.getPrecio() == null || tamanoInfo.getPrecio() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del tamaño es obligatorio y debe ser mayor o igual a 0.");
                }
                
                // Encontrar el precio más bajo para el precio base del producto
                if (tamanoInfo.getPrecio() < precioMasBajo) {
                    precioMasBajo = tamanoInfo.getPrecio();
                }
                
                try {
                    Tamano tamano = tamanoRepository.findById(tamanoInfo.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + tamanoInfo.getId()));
                    
                    // Verificar que el tamaño pertenece al negocio del usuario
                    if (currentUser.getRole() != Role.ADMIN && !tamano.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar tamaños de otros negocios.");
                    }
                    
                    // Verificar que no existe ya una relación para este producto y tamaño
                    if (productoTamanoRepository.existsByProductoIdAndTamanoId(savedProducto.getId(), tamanoInfo.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una relación para este producto y tamaño.");
                    }
                    
                    ProductoTamano productoTamano = new ProductoTamano();
                    productoTamano.setProducto(savedProducto);
                    productoTamano.setTamano(tamano);
                    productoTamano.setPrecio(tamanoInfo.getPrecio());
                    
                    // Guardar la relación directamente
                    ProductoTamano savedProductoTamano = productoTamanoRepository.save(productoTamano);
                    
                } catch (Exception e) {
                    throw e;
                }
            }
            
            // Actualizar el precio base del producto con el precio más bajo de los tamaños
            if (precioMasBajo != Double.MAX_VALUE) {
                savedProducto.setPrecio(precioMasBajo);
                productoRepository.save(savedProducto); // Guardar el producto actualizado
            }
        }
        
        try {
            ProductoDTO result = convertToDTO(savedProducto);
            return result;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error al crear el producto: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al crear el producto: " + e.getMessage());
        }
    }

    public ProductoDTO updateProducto(Long id, ProductoDTO productoDTO) {
        User currentUser = getCurrentUser();
        Producto existingProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto not found with id: " + id));

        tieneAccesoProducto(currentUser, existingProducto);

        // Validaciones básicas
        if (productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio.");
        }
        
        if (productoDTO.getPrecio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto es obligatorio.");
        }
        if (productoDTO.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto debe ser mayor o igual a 0.");
        }

        // Actualizar campos básicos
        existingProducto.setNombre(productoDTO.getNombre().trim());
        existingProducto.setPrecio(productoDTO.getPrecio());
        existingProducto.setImagenUrl(productoDTO.getImagenUrl());
        if (productoDTO.getActivo() != null) {
            existingProducto.setActivo(productoDTO.getActivo());
        }
        
        // Actualizar subcategoría solo si se proporciona
        if (productoDTO.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(productoDTO.getSubcategoriaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría not found with id: " + productoDTO.getSubcategoriaId()));
            existingProducto.setSubcategoria(subcategoria);
        }
        
        // Manejar la actualización de sucursales solo si se proporciona
        if (productoDTO.getSucursales() != null) {
            // Obtener las sucursales actuales del producto
            Set<Long> currentSucursalIds = existingProducto.getSucursales().stream()
                    .map(ps -> ps.getSucursal().getId())
                    .collect(Collectors.toSet());
            
            // Obtener las nuevas sucursales del DTO
            Set<Long> newSucursalIds = new HashSet<>();
            if (!productoDTO.getSucursales().isEmpty()) {
                for (ProductoDTO.SucursalInfo sucursalInfo : productoDTO.getSucursales()) {
                    // Verificar que no hay duplicados
                    if (!newSucursalIds.add(sucursalInfo.getId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal duplicada: " + sucursalInfo.getId());
                    }
                    
                    Sucursal sucursal = sucursalRepository.findById(sucursalInfo.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + sucursalInfo.getId()));
                    
                    // Verificar que la sucursal pertenece al negocio del usuario
                    if (currentUser.getRole() != Role.ADMIN && !sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar productos a sucursales de otros negocios.");
                    }
                }
            } else {
                // Si no se especifican sucursales, usar todas las del negocio
                List<Sucursal> sucursalesNegocio = sucursalRepository.findByNegocioId(existingProducto.getNegocio().getId());
                for (Sucursal sucursal : sucursalesNegocio) {
                    newSucursalIds.add(sucursal.getId());
                }
            }
            
            // Eliminar relaciones que ya no existen
            existingProducto.getSucursales().removeIf(ps -> !newSucursalIds.contains(ps.getSucursal().getId()));
            
            // Agregar nuevas relaciones
            for (Long sucursalId : newSucursalIds) {
                if (!currentSucursalIds.contains(sucursalId)) {
                    Sucursal sucursal = sucursalRepository.findById(sucursalId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + sucursalId));
                    
                    ProductoSucursal productoSucursal = new ProductoSucursal();
                    productoSucursal.setProducto(existingProducto);
                    productoSucursal.setSucursal(sucursal);
                    productoSucursal.setActivo(true);
                    existingProducto.getSucursales().add(productoSucursal);
                }
            }
        }
        
        // Manejar la actualización de tamaños solo si se proporciona
        if (productoDTO.getTamanos() != null) {
            // Obtener los tamaños actuales del producto
            Set<Long> currentTamanoIds = existingProducto.getProductoTamanos().stream()
                    .map(pt -> pt.getTamano().getId())
                    .collect(Collectors.toSet());
            
            // Obtener los nuevos tamaños del DTO
            Set<Long> newTamanoIds = new HashSet<>();
            Double precioMasBajo = Double.MAX_VALUE;
            
            if (!productoDTO.getTamanos().isEmpty()) {
                for (ProductoDTO.TamanoInfo tamanoInfo : productoDTO.getTamanos()) {
                    // Verificar que no hay duplicados
                    if (!newTamanoIds.add(tamanoInfo.getId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tamaño duplicado: " + tamanoInfo.getId());
                    }
                    
                    // Validar precio del tamaño
                    if (tamanoInfo.getPrecio() == null || tamanoInfo.getPrecio() < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del tamaño es obligatorio y debe ser mayor o igual a 0.");
                    }
                    
                    // Encontrar el precio más bajo para el precio base del producto
                    if (tamanoInfo.getPrecio() < precioMasBajo) {
                        precioMasBajo = tamanoInfo.getPrecio();
                    }
                    
                    Tamano tamano = tamanoRepository.findById(tamanoInfo.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + tamanoInfo.getId()));
                    
                    // Verificar que el tamaño pertenece al negocio del usuario
                    if (currentUser.getRole() != Role.ADMIN && !tamano.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar tamaños de otros negocios.");
                    }
                }
            }
            
            // Eliminar relaciones de tamaños que ya no existen
            existingProducto.getProductoTamanos().removeIf(pt -> !newTamanoIds.contains(pt.getTamano().getId()));
            
            // Agregar nuevas relaciones de tamaños
            for (ProductoDTO.TamanoInfo tamanoInfo : productoDTO.getTamanos()) {
                if (!currentTamanoIds.contains(tamanoInfo.getId())) {
                    Tamano tamano = tamanoRepository.findById(tamanoInfo.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + tamanoInfo.getId()));
                    
                    ProductoTamano productoTamano = new ProductoTamano();
                    productoTamano.setProducto(existingProducto);
                    productoTamano.setTamano(tamano);
                    productoTamano.setPrecio(tamanoInfo.getPrecio());
                    existingProducto.getProductoTamanos().add(productoTamano);
                } else {
                    // Actualizar precio del tamaño existente
                    existingProducto.getProductoTamanos().stream()
                            .filter(pt -> pt.getTamano().getId().equals(tamanoInfo.getId()))
                            .findFirst()
                            .ifPresent(pt -> pt.setPrecio(tamanoInfo.getPrecio()));
                }
            }
            
            // Actualizar el precio base del producto con el precio más bajo de los tamaños
            if (precioMasBajo != Double.MAX_VALUE) {
                existingProducto.setPrecio(precioMasBajo);
            }
        }

        try {
            return convertToDTO(productoRepository.save(existingProducto));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error al actualizar el producto: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al actualizar el producto: " + e.getMessage());
        }
    }

    public void deleteProducto(Long id) {
        User currentUser = getCurrentUser();
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto not found with id: " + id));

        tieneAccesoProducto(currentUser, producto);
        
        try {
            // Eliminar el producto (las relaciones se eliminarán automáticamente por cascada)
            productoRepository.deleteById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al eliminar el producto: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (email == null || email.equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return user;
    }

    private ProductoDTO convertToDTO(Producto producto) {
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setId(producto.getId());
        productoDTO.setNombre(producto.getNombre());
        productoDTO.setPrecio(producto.getPrecio());
        productoDTO.setImagenUrl(producto.getImagenUrl());
        productoDTO.setActivo(producto.getActivo());
        
        if (producto.getNegocio() != null) {
            productoDTO.setNegocioId(producto.getNegocio().getId());
            productoDTO.setNegocioNombre(producto.getNegocio().getNombre());
        }
        
        if (producto.getSubcategoria() != null) {
            productoDTO.setSubcategoriaId(producto.getSubcategoria().getId());
            productoDTO.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }
        
        if (producto.getSucursales() != null) {
            List<ProductoDTO.SucursalInfo> sucursalesInfo = producto.getSucursales().stream()
                    .map(ps -> new ProductoDTO.SucursalInfo(ps.getSucursal().getId(), ps.getSucursal().getNombre()))
                    .distinct() // Eliminar duplicados basándose en equals/hashCode
                    .collect(Collectors.toList());
            productoDTO.setSucursales(sucursalesInfo);
        }
        
        // Cargar tamaños desde la base de datos
        List<ProductoTamano> productoTamanos = productoTamanoRepository.findByProductoId(producto.getId());
        
        if (!productoTamanos.isEmpty()) {
            List<ProductoDTO.TamanoInfo> tamanosInfo = productoTamanos.stream()
                    .map(pt -> {
                        return new ProductoDTO.TamanoInfo(
                                pt.getTamano().getId(), 
                                pt.getTamano().getNombre(), 
                                pt.getTamano().getDescripcion(), 
                                pt.getPrecio());
                    })
                    .distinct()
                    .collect(Collectors.toList());
            productoDTO.setTamanos(tamanosInfo);
        }
        
        return productoDTO;
    }

    private void tieneAccesoProducto(User currentUser, Producto producto) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // ADMIN puede acceder a cualquier producto
        } else if (currentUser.getRole() == Role.OWNER && 
                   currentUser.getNegocio() != null && 
                   producto.getNegocio() != null &&
                   currentUser.getNegocio().getId().equals(producto.getNegocio().getId())) {
            return; // OWNER puede acceder solo a productos de su negocio
        } else if (currentUser.getRole() == Role.SUCURSAL && 
                   currentUser.getSucursal() != null && 
                   producto.getSucursales() != null &&
                   producto.getSucursales().stream()
                       .anyMatch(ps -> ps.getSucursal().getId().equals(currentUser.getSucursal().getId()))) {
            return; // SUCURSAL puede acceder solo a productos de su sucursal
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER de su negocio y SUCURSAL de su sucursal pueden acceder.");
        }
    }
} 