package com.torder.tamano;

import java.util.ArrayList;
import java.util.List;

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
    private List<Long> sucursalesIds = new ArrayList<>();
} 