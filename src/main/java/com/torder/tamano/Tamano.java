package com.torder.tamano;

import java.util.HashSet;
import java.util.Set;

import com.torder.producto.Producto;
import com.torder.relaciones.ProductoTamano;
import com.torder.relaciones.TamanoSucursal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"productos", "sucursales"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tamanos")
public class Tamano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;
    
    @OneToMany(mappedBy = "tamano", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoTamano> productos = new HashSet<>();
    
    @OneToMany(mappedBy = "tamano", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TamanoSucursal> sucursales = new HashSet<>();
    
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
}
