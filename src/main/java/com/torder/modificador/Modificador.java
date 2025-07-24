package com.torder.modificador;

import java.util.HashSet;
import java.util.Set;

import com.torder.relaciones.ModificadorSucursal;
import com.torder.subcategoria.Subcategoria;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "modificadores", uniqueConstraints = {
})
public class Modificador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private Subcategoria subcategoria;
    
    @OneToMany(mappedBy = "modificador", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModificadorSucursal> sucursales = new HashSet<>();
    
    // Método para establecer el nombre en mayúsculas
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.toUpperCase() : null;
    }
}