package com.torder.subcategoria;


import java.util.ArrayList;
import java.util.List;

import com.torder.modificador.Modificador;
import com.torder.producto.Producto;
import com.torder.sucursal.Sucursal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Subcategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;


    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @OneToMany(mappedBy = "subcategoria")
    private List<Producto> productos = new ArrayList<>();


    @OneToMany(mappedBy = "subcategoria")
    private List<Modificador> modificadores = new ArrayList<>();


}
