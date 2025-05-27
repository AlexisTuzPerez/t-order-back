package com.torder.mesa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MesaDTO {
    private Long id;
    private Integer numero;
    private Long sucursalId;
    private String sucursalNombre;
}