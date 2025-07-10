package com.torder.relaciones;

import lombok.Data;

@Data
public class OrdenProductoModificadorDTO {
    private Long id;
    private Long ordenProductoId;
    private Long modificadorId;
    private String modificadorNombre;
    private Double precioModificador;
    private Integer cantidad;
}