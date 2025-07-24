package com.torder.orden.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.torder.orden.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponse {
    
    private Long id;
    private Status estado;
    private String notas;
    private Double total;
    private LocalDateTime fechaCreacion;
    private String usuarioNombre;
    private String mesaNombre;
    private List<ProductoOrdenResponse> productos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoOrdenResponse {
        private Long id;
        private String productoNombre;
        private String tamanoNombre;
        private Double precio;
        private Integer cantidad;
        private Double subtotal;
        private List<ModificadorOrdenResponse> modificadores;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModificadorOrdenResponse {
        private Long id;
        private String modificadorNombre;
        private Double precioModificador;
        private Integer cantidad;
        private Double subtotal;
    }
} 