
package com.torder.sucursal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.torder.mesa.Mesa;
import com.torder.negocioCliente.NegocioCliente;
import com.torder.relaciones.DescuentoSucursal;
import com.torder.relaciones.ProductoSucursal;
import com.torder.subcategoria.Subcategoria;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"mesas", "productos", "subcategorias", "descuentos"})
@EqualsAndHashCode(exclude = {"mesas", "productos", "subcategorias", "descuentos"})
@Entity
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 50, message = "La ciudad no puede exceder los 50 caracteres")
    private String cuidad;

    @NotBlank(message = "El estado es obligatorio")
    @Size(min = 2, max = 30, message = "El estado debe tener entre 2 y 30 caracteres")
    private String estado;

    private Boolean activo;

    @NotNull(message = "Debe pertenecer a un negocio")
    @ManyToOne
    @JoinColumn(name = "negocio_id")
    private NegocioCliente negocio;

    @OneToMany(mappedBy = "sucursal")
    private List<Mesa> mesas;

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoSucursal> productos = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    private List<Subcategoria> subcategorias;

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DescuentoSucursal> descuentos = new HashSet<>();
}