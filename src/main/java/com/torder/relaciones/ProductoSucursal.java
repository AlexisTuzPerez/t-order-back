package com.torder.relaciones;

import com.torder.producto.Producto;
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
@Table(name = "producto_sucursales")
public class ProductoSucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Boolean activo;



    
    // Métodos de ayuda para la relación bidireccional
    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null && !producto.getSucursales().contains(this)) {
            producto.getSucursales().add(this);
        }
    }
    
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
        if (sucursal != null && !sucursal.getProductos().contains(this)) {
            sucursal.getProductos().add(this);
        }
    }
}
