package com.torder.relaciones;

import com.torder.descuento.Descuento;
import com.torder.orden.Orden;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orden_descuento")
@Data
public class OrdenDescuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "descuento_id", nullable = false)
    private Descuento descuento;

    @Column(name = "monto_descontado", nullable = false)
    private Double montoDescontado;

    @Column(name = "motivo")
    private String motivo; // "Descuento Happy Hour", "2x1 en hamburguesas", "Cupón WELCOME20", etc.

    @Column(name = "productos_aplicados")
    private String productosAplicados; // IDs de productos afectados por el descuento

    @Column(name = "nombre_descuento")
    private String nombreDescuento; // El nombre del descuento aplicado

    // Métodos de ayuda

   
} 