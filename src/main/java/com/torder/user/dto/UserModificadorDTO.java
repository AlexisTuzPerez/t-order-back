package com.torder.user.dto;

import lombok.Data;

@Data
public class UserModificadorDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Long subcategoriaId;
    private String subcategoriaNombre;
} 