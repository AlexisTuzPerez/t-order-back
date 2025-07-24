package com.torder.orden.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearOrdenRequest {
    
    @NotNull(message = "El ID de la sucursal es requerido")
    @Positive(message = "El ID de la sucursal debe ser positivo")
    private Long sucursalId;
    
    @NotNull(message = "El ID de la mesa es requerido")
    @Positive(message = "El ID de la mesa debe ser positivo")
    private Long mesaId;
    
    private String notas;
    
    @Valid
    @NotNull(message = "La lista de productos es requerida")
    private List<ProductoOrdenRequest> productos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoOrdenRequest {
        
        @NotNull(message = "El ID del producto es requerido")
        @Positive(message = "El ID del producto debe ser positivo")
        private Long productoId;
        
        @NotNull(message = "El ID del tamaño es requerido")
        @Positive(message = "El ID del tamaño debe ser positivo")
        private Long tamanoId;
        
        @NotNull(message = "La cantidad es requerida")
        @Positive(message = "La cantidad debe ser positiva")
        private Integer cantidad;
        
        @Valid
        private List<ModificadorOrdenRequest> modificadores;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModificadorOrdenRequest {
        
        @NotNull(message = "El ID del modificador es requerido")
        @Positive(message = "El ID del modificador debe ser positivo")
        private Long modificadorId;
    }
} 