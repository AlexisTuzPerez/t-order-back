package com.torder.subcategoria;


import com.torder.modificador.Modificador;
import com.torder.producto.Producto;
import com.torder.sucursal.Sucursal;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Subcategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @OneToMany(mappedBy = "subcategoria")
    private List<Producto> productos = new ArrayList<>();


    @OneToMany(mappedBy = "subcategoria")
    private List<Modificador> modificadores = new ArrayList<>();


}
