package com.torder.orden;

import java.util.ArrayList;
import java.util.List;

import com.torder.mesa.Mesa;
import com.torder.relaciones.OrdenDescuento;
import com.torder.relaciones.OrdenProducto;
import com.torder.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "orden")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status estado;

    private String notas;

    @Column(name = "subtotal")
    private Double subtotal; // Total antes de descuentos

    @Column(name = "total_descuentos")
    private Double totalDescuentos = 0.0; // Suma de todos los descuentos aplicados (automáticos + cupones)

    @Column(name = "total_final")
    private Double totalFinal; // Total después de descuentos

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenProducto> items = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDescuento> descuentos = new ArrayList<>();
}

