package com.torder.relaciones;

import java.util.List;

import lombok.Data;

@Data
public class OrdenProductoDTO {
    private Long id;
    private Long ordenId;
    private Long productoId;
    private String productoNombre;
    private String productoImagenUrl;
    private Long tamanoId;
    private String tamanoNombre;
    private Double precioBase; // Precio base del Producto
    private Double precioTamaño; // Precio del tamaño (se calculará en el servicio)
    private Double precioUnitario; // Precio unitario (base + tamaño + modificadores)
    private Double precioTotal; // Precio total (unitario * cantidad)
    private Integer cantidad;
    private List<OrdenProductoModificadorDTO> modificadores;
} 