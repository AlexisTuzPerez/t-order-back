package com.torder.subcategoria;

import java.util.ArrayList;
import java.util.List;

import com.torder.modificador.Modificador;
import com.torder.producto.Producto;
import com.torder.relaciones.SubcategoriaSucursal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = {"productos", "modificadores", "sucursales"})
@EqualsAndHashCode(exclude = {"productos", "modificadores", "sucursales"})
public class Subcategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "subcategoria")
    private List<SubcategoriaSucursal> sucursales = new ArrayList<>();

    @OneToMany(mappedBy = "subcategoria")
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "subcategoria")
    private List<Modificador> modificadores = new ArrayList<>();
}
