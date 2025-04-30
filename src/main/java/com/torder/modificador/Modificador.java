package com.torder.modificador;

import com.torder.subcategoria.Subcategoria;
import jakarta.persistence.*;

@Entity
public class Modificador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precio;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;



}