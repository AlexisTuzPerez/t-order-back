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
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"producto", "sucursal"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "producto_sucursales", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"producto_id", "sucursal_id"})
})
public class ProductoSucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
        // Comentado para evitar duplicados en la relación bidireccional
        // if (producto != null && !producto.getSucursales().contains(this)) {
        //     producto.getSucursales().add(this);
        // }
    }
    
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
        // Comentado para evitar duplicados en la relación bidireccional
        // if (sucursal != null && !sucursal.getProductos().contains(this)) {
        //     sucursal.getProductos().add(this);
        // }
    }
}
