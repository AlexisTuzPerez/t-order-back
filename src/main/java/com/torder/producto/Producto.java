package com.torder.producto;

import java.util.HashSet;
import java.util.Set;

import com.torder.negocioCliente.NegocioCliente;
import com.torder.relaciones.ProductoSucursal;
import com.torder.relaciones.ProductoTamano;
import com.torder.subcategoria.Subcategoria;

import jakarta.persistence.CascadeType;
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
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Boolean activo = true;
    private String imagenUrl;
    private Double precio;

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private NegocioCliente negocio;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoSucursal> sucursales = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoTamano> productoTamanos = new HashSet<>();

}
