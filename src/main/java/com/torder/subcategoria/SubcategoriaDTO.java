package com.torder.subcategoria;

import lombok.Data;

@Data
public class SubcategoriaDTO {
    private Long id;
    private String nombre;
    private Long sucursalId;
    private String sucursalNombre;
}