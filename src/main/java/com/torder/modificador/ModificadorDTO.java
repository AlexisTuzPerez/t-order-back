package com.torder.modificador;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModificadorDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Long subcategoriaId;
    private String subcategoriaNombre;
    private List<Long> sucursalesIds = new ArrayList<>();
} 