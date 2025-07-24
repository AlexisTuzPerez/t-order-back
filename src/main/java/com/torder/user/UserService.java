package com.torder.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.torder.mesa.Mesa;
import com.torder.modificador.Modificador;
import com.torder.producto.Producto;
import com.torder.relaciones.ProductoTamano;
import com.torder.subcategoria.Subcategoria;
import com.torder.sucursal.Sucursal;
import com.torder.tamano.ProductoTamanoRepository;
import com.torder.user.dto.UserMesaDTO;
import com.torder.user.dto.UserModificadorDTO;
import com.torder.user.dto.UserProductoDTO;
import com.torder.user.dto.UserSubcategoriaDTO;
import com.torder.user.dto.UserSucursalDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProductoTamanoRepository productoTamanoRepository;

    public UserProductoDTO convertToUserProductoDTO(Producto producto) {
        UserProductoDTO dto = new UserProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setPrecio(producto.getPrecio());
        
        if (producto.getSubcategoria() != null) {
            dto.setSubcategoriaId(producto.getSubcategoria().getId());
            dto.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }
        
        // Obtener los tamaños de este producto
        List<ProductoTamano> productoTamanos = productoTamanoRepository.findByProductoId(producto.getId());
        List<UserProductoDTO.TamanoInfo> tamanosInfo = productoTamanos.stream()
                .map(pt -> {
                    UserProductoDTO.TamanoInfo tamanoInfo = new UserProductoDTO.TamanoInfo();
                    tamanoInfo.setId(pt.getTamano().getId());
                    tamanoInfo.setNombre(pt.getTamano().getNombre());
                    tamanoInfo.setDescripcion(pt.getTamano().getDescripcion());
                    tamanoInfo.setPrecio(pt.getPrecio());
                    return tamanoInfo;
                })
                .collect(Collectors.toList());
        
        dto.setTamaños(tamanosInfo);
        
        return dto;
    }

    public UserModificadorDTO convertToUserModificadorDTO(Modificador modificador) {
        UserModificadorDTO dto = new UserModificadorDTO();
        dto.setId(modificador.getId());
        dto.setNombre(modificador.getNombre());
        dto.setPrecio(modificador.getPrecio());
        
        if (modificador.getSubcategoria() != null) {
            dto.setSubcategoriaId(modificador.getSubcategoria().getId());
            dto.setSubcategoriaNombre(modificador.getSubcategoria().getNombre());
        }
        
        return dto;
    }

    public UserSubcategoriaDTO convertToUserSubcategoriaDTO(Subcategoria subcategoria) {
        UserSubcategoriaDTO dto = new UserSubcategoriaDTO();
        dto.setId(subcategoria.getId());
        dto.setNombre(subcategoria.getNombre());
        return dto;
    }

    public UserSucursalDTO convertToUserSucursalDTO(Sucursal sucursal) {
        UserSucursalDTO dto = new UserSucursalDTO();
        dto.setId(sucursal.getId());
        dto.setNombre(sucursal.getNombre());
        dto.setCiudad(sucursal.getCuidad()); // Nota: hay un typo en el campo original
        dto.setEstado(sucursal.getEstado());
        return dto;
    }

    public UserMesaDTO convertToUserMesaDTO(Mesa mesa) {
        UserMesaDTO dto = new UserMesaDTO();
        dto.setId(mesa.getId());
        dto.setNumero(mesa.getNumero());
        return dto;
    }
} 