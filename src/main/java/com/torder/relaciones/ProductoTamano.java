package com.torder.relaciones;

import com.torder.producto.Producto;
import com.torder.tamano.Tamano;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ProductoTamanoId id = new ProductoTamanoId();
    
    @ManyToOne
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @ManyToOne
    @MapsId("tamanoId")
    @JoinColumn(name = "tamano_id", nullable = false)
    private Tamano tamano;
    
    @Column(nullable = false)
    private Double precio;
    
    // Métodos de ayuda para la relación bidireccional
    public void setProducto(Producto producto) {
        this.producto = producto;
        if (this.id == null) {
            this.id = new ProductoTamanoId();
        }
        if (producto != null) {
            this.id.setProductoId(producto.getId());
        }
    }
    
    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
        if (this.id == null) {
            this.id = new ProductoTamanoId();
        }
        if (tamano != null) {
            this.id.setTamanoId(tamano.getId());
        }
    }
}
