package com.torder.tamano;

import java.util.HashSet;
import java.util.Set;

import com.torder.negocioCliente.NegocioCliente;
import com.torder.producto.Producto;
import com.torder.relaciones.ProductoTamano;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"productos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tamanos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "negocio_id"})
})
public class Tamano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private NegocioCliente negocio;
    
    @OneToMany(mappedBy = "tamano", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoTamano> productos = new HashSet<>();
    
    // Método de ayuda para agregar un producto a este tamaño
    public void addProducto(Producto producto, Double precio) {
        ProductoTamano productoTamano = new ProductoTamano();
        productoTamano.setTamano(this);
        productoTamano.setProducto(producto);
        productoTamano.setPrecio(precio);
        productos.add(productoTamano);
    }
    
    // Método de ayuda para remover un producto de este tamaño
    public void removeProducto(Producto producto) {
        productos.removeIf(pt -> {
            if (pt.getProducto().equals(producto)) {
                pt.setTamano(null);
                return true;
            }
            return false;
        });
    }
    
    // Método para establecer el nombre en mayúsculas
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.toUpperCase() : null;
    }
}
