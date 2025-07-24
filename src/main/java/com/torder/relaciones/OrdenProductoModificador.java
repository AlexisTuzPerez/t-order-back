package com.torder.relaciones;

import com.torder.modificador.Modificador;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orden_producto_modificador")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
} 