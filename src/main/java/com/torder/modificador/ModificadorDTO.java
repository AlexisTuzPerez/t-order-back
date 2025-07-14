package com.torder.modificador;

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
    private String subcategoriaNombre;
} 