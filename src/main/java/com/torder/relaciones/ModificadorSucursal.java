package com.torder.relaciones;

import com.torder.modificador.Modificador;
import com.torder.sucursal.Sucursal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"modificador", "sucursal"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "modificador_sucursales", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"modificador_id", "sucursal_id"})
})
public class ModificadorSucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modificador_id", nullable = false)
    private Modificador modificador;
    
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Boolean activo = true;
} 