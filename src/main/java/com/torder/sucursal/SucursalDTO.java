package com.torder.sucursal;


import lombok.Data;

@Data
public class SucursalDTO {
    private Long id;
    private String nombre;
    private String cuidad;
    private String estado;
    private Long negocioId;


}