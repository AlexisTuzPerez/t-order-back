package com.torder.producto;

import com.torder.mesa.Mesa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, SucursalRepository sucursalRepository, SubcategoriaRepository subcategoriaRepository) {
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
        this.subcategoriaRepository = subcategoriaRepository;
    }

    public Page<ProductoDTO> getAllProductos(Pageable pageable) {


        Page<Producto> productos = productoRepository.findAll(pageable);
        return productos.map(this::convertToDto);
    }

    public ProductoDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto not found"));
        return convertToDto(producto);
    }

    public ProductoDTO createProducto(ProductoDTO productoDTO) {
        Producto producto = convertToEntity(productoDTO);
        producto = productoRepository.save(producto);
        return convertToDto(producto);
    }

    public ProductoDTO updateProducto(Long id, ProductoDTO productoDTO) {
        Producto existingProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto not found"));

        existingProducto.setNombre(productoDTO.getNombre());
        existingProducto.setPrecio(productoDTO.getPrecio());

        if (productoDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(productoDTO.getSucursalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found"));
            existingProducto.setSucursal(sucursal);
        }

        if (productoDTO.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(productoDTO.getSubcategoriaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));
            existingProducto.setSubcategoria(subcategoria);
        }

        Producto updatedProducto = productoRepository.save(existingProducto);
        return convertToDto(updatedProducto);
    }

    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }

    private ProductoDTO convertToDto(Producto producto) {
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setId(producto.getId());
        productoDTO.setNombre(producto.getNombre());
        productoDTO.setPrecio(producto.getPrecio());
        if (producto.getSucursal() != null) {
            productoDTO.setSucursalId(producto.getSucursal().getId());
            productoDTO.setSucursalNombre(producto.getSucursal().getNombre());
        }
        if (producto.getSubcategoria() != null) {
            productoDTO.setSubcategoriaId(producto.getSubcategoria().getId());
            productoDTO.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }
        return productoDTO;
    }

    private Producto convertToEntity(ProductoDTO productoDTO) {
        Producto producto = new Producto();
        producto.setId(productoDTO.getId());
        producto.setNombre(productoDTO.getNombre());
        producto.setPrecio(productoDTO.getPrecio());

        if (productoDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(productoDTO.getSucursalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found"));
            producto.setSucursal(sucursal);
        }

        if (productoDTO.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(productoDTO.getSubcategoriaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));
            producto.setSubcategoria(subcategoria);
        }
        return producto;
    }
}