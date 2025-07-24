package com.torder.relaciones;

import java.util.ArrayList;
import java.util.List;

import com.torder.orden.Orden;
import com.torder.producto.Producto;
import com.torder.tamano.Tamano;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_producto")
public class OrdenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "tamano_id", nullable = false)
    private Tamano tamano;

    private Double precio;
    private Integer cantidad;

    @OneToMany(mappedBy = "ordenProducto", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrdenProductoModificador> modificadores = new ArrayList<>();
}
