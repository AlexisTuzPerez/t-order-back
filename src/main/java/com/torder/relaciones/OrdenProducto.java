package com.torder.relaciones;

import java.util.ArrayList;
import java.util.List;

import com.torder.modificador.Modificador;
import com.torder.orden.Orden;
import com.torder.producto.Producto;
import com.torder.tamano.Tamano;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orden_producto")
public class OrdenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "tamano_id")
    private Tamano tamano;

    @OneToMany(mappedBy = "ordenProducto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenProductoModificador> modificadores = new ArrayList<>();

    private Integer cantidad;

    // Constructors
    public OrdenProducto() {}

    public OrdenProducto(Orden orden, Producto producto, Tamano tamano, Integer cantidad) {
        this.orden = orden;
        this.producto = producto;
        this.tamano = tamano;
        this.cantidad = cantidad;
    }

    // Helper methods for modificadores
    public void agregarModificador(Modificador modificador, Double precioModificador, Integer cantidad) {
        OrdenProductoModificador opm = new OrdenProductoModificador(this, modificador, precioModificador, cantidad);
        modificadores.add(opm);
    }

    public void eliminarModificador(Modificador modificador) {
        modificadores.removeIf(opm -> opm.getModificador().equals(modificador));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Tamano getTamano() {
        return tamano;
    }

    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
    }

    public List<OrdenProductoModificador> getModificadores() {
        return modificadores;
    }

    public void setModificadores(List<OrdenProductoModificador> modificadores) {
        this.modificadores = modificadores;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
