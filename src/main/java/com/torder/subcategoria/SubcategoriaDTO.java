package com.torder.subcategoria;

import java.util.List;

import lombok.Data;

@Data
public class SubcategoriaDTO {
    private Long id;
    private String nombre;
    private List<Long> sucursalesIds;
}