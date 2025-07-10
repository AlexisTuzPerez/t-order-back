package com.torder.relaciones;

import com.torder.descuento.Descuento;
import com.torder.sucursal.Sucursal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "descuento_sucursales")
public class DescuentoSucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "descuento_id", nullable = false)
    private Descuento descuento;
    
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Boolean activo = true;

        
    // Métodos de ayuda para la relación bidireccional
    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
        if (descuento != null && !descuento.getSucursales().contains(this)) {
            descuento.getSucursales().add(this);
        }
    }
    
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
        if (sucursal != null && !sucursal.getDescuentos().contains(this)) {
            sucursal.getDescuentos().add(this);
        }
    }

} 