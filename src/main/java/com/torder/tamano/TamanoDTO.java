package com.torder.tamano;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TamanoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String negocioNombre;
} 