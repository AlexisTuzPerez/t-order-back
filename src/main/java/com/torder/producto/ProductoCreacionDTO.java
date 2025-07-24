package com.torder.producto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoCreacionDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private Boolean activo = true;
    private String imagenUrl;
    
    @NotNull(message = "El precio es obligatorio")
    private Double precio;
    
    @NotNull(message = "La subcategoría es obligatoria")
    private Long subcategoriaId;
    
    private List<TamanoCreacion> tamaños = new ArrayList<>();
    
    @Data
    public static class TamanoCreacion {
        @NotNull(message = "El ID del tamaño es obligatorio")
        private Long tamanoId;
        
        @NotNull(message = "El precio del tamaño es obligatorio")
        private Double precio;
        
        public TamanoCreacion() {}
        
        public TamanoCreacion(Long tamanoId, Double precio) {
            this.tamanoId = tamanoId;
            this.precio = precio;
        }
    }
} 