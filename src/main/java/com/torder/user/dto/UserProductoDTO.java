package com.torder.user.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserProductoDTO {
    private Long id;
    private String nombre;
    private String imagenUrl;
    private Double precio;
    private Long subcategoriaId;
    private String subcategoriaNombre;
    private List<TamanoInfo> tamaños;
    
    @Data
    public static class TamanoInfo {
        private Long id;
        private String nombre;
        private String descripcion;
        private Double precio;
    }
} 