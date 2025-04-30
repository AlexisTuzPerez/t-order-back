package com.torder.negocioCliente;

import lombok.Data;

import java.util.List;


@Data
public class NegocioDTO {
    private Long id;
    private String nombre;
    private String telefono;
    private String mail;
    private List<Long> sucursalesIds;
    private List<Long> usuariosIds;

}