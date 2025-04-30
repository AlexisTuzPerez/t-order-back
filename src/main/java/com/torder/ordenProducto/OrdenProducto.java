package com.torder.ordenProducto;


import com.torder.modificador.Modificador;
import com.torder.orden.Orden;
import com.torder.producto.Producto;
import jakarta.persistence.*;

@Entity
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
    @JoinColumn(name = "modificador_id")
    private Modificador modificador;

    private Integer cantidad;

}
