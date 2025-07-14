package com.torder.modificador;

import com.torder.subcategoria.Subcategoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "modificadores", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "subcategoria_id"})
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
    
    // Método para establecer el nombre en mayúsculas
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.toUpperCase() : null;
    }
}