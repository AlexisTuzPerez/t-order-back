package com.torder.producto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
    private String imagenUrl;
    private Long negocioId;
    private String negocioNombre;
    private List<Long> sucursalesIds = new ArrayList<>();
    private Long subcategoriaId;
    private String subcategoriaNombre;
}