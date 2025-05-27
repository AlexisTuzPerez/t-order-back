package com.torder.producto;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Long sucursalId;
    private String sucursalNombre;
    private Long subcategoriaId;
    private String subcategoriaNombre;
}