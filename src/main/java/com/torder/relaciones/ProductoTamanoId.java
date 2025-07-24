package com.torder.relaciones;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProductoTamanoId implements Serializable {
    
    @Column(name = "producto_id")
    private Long productoId;
    
    @Column(name = "tamano_id")
    private Long tamanoId;
} 