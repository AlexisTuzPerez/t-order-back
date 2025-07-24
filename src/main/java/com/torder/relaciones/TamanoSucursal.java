package com.torder.relaciones;

import com.torder.sucursal.Sucursal;
import com.torder.tamano.Tamano;

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
@ToString(exclude = {"tamano", "sucursal"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tamano_sucursales", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tamano_id", "sucursal_id"})
})
public class TamanoSucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tamano_id", nullable = false)
    private Tamano tamano;
    
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Boolean activo = true;
} 