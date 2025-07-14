package com.torder.relaciones;

import com.torder.producto.Producto;
import com.torder.tamano.Tamano;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"producto", "tamano"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "producto_tamanos")
public class ProductoTamano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "tamano_id", nullable = false)
    private Tamano tamano;
    
    @Column(nullable = false)
    private Double precio;
    
    // Métodos de ayuda para la relación bidireccional
    public void setProducto(Producto producto) {
        this.producto = producto;
        // Comentado para evitar duplicados en la relación bidireccional
        // if (producto != null) {
        //     producto.getProductoTamanos().add(this);
        // }
    }
    
    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
        // Comentado para evitar duplicados en la relación bidireccional
        // if (tamano != null) {
        //     tamano.getProductos().add(this);
        // }
    }
}
