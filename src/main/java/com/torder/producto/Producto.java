package com.torder.producto;


import com.torder.subcategoria.Subcategoria;
import com.torder.sucursal.Sucursal;
import jakarta.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precio;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;



    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;
}