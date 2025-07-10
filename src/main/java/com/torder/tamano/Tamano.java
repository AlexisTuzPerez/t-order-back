package com.torder.tamano;

import com.torder.relaciones.ProductoTamano;
import jakarta.persistence.*;
import lombok.Data;
import com.torder.negocioCliente.NegocioCliente;
import com.torder.producto.Producto;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "tamanos")
public class Tamano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
