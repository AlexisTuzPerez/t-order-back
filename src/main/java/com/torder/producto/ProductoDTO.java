package com.torder.producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Boolean activo = true;
    private String imagenUrl;
    private Double precio;
    private List<Long> sucursalesIds = new ArrayList<>();
    private Long subcategoriaId;
    private String subcategoriaNombre;
    private List<TamanoInfo> tamanos = new ArrayList<>();
    
    @Data
    public static class TamanoInfo {
        private Long id;
        private String nombre;
        private String descripcion;
        private Double precio;
        
        public TamanoInfo() {}
        
        public TamanoInfo(Long id, String nombre, String descripcion, Double precio) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
        }
        
        // Constructor simplificado para crear relaciones
        public TamanoInfo(Long id, Double precio) {
            this.id = id;
            this.precio = precio;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TamanoInfo that = (TamanoInfo) o;
            return Objects.equals(id, that.id);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}