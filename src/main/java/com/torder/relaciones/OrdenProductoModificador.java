package com.torder.relaciones;

import com.torder.modificador.Modificador;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orden_producto_modificador")
public class OrdenProductoModificador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_producto_id", nullable = false)
    private OrdenProducto ordenProducto;

    @ManyToOne
    @JoinColumn(name = "modificador_id", nullable = false)
    private Modificador modificador;

    private Double precioModificador;
    private Integer cantidad;

    // Constructors
    public OrdenProductoModificador() {}

    public OrdenProductoModificador(OrdenProducto ordenProducto, Modificador modificador, Double precioModificador, Integer cantidad) {
        this.ordenProducto = ordenProducto;
        this.modificador = modificador;
        this.precioModificador = precioModificador;
        this.cantidad = cantidad;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrdenProducto getOrdenProducto() {
        return ordenProducto;
    }

    public void setOrdenProducto(OrdenProducto ordenProducto) {
        this.ordenProducto = ordenProducto;
    }

    public Modificador getModificador() {
        return modificador;
    }

    public void setModificador(Modificador modificador) {
        this.modificador = modificador;
    }

    public Double getPrecioModificador() {
        return precioModificador;
    }

    public void setPrecioModificador(Double precioModificador) {
        this.precioModificador = precioModificador;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
} 