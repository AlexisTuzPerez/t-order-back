package com.torder.sucursal;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SucursalDTO {
    private Long id;
    private String nombre;
    private String cuidad;
    private String estado;
    private Boolean activo;
    private Long negocioId;



}