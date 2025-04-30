
package com.torder.sucursal;
import com.torder.mesa.Mesa;
import com.torder.negocioCliente.NegocioCliente;
import com.torder.producto.Producto;
import com.torder.subcategoria.Subcategoria;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
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

    @NotNull(message = "Debe pertenecer a un negocio")
    @ManyToOne
    @JoinColumn(name = "negocio_id")
    private NegocioCliente negocio;


    @OneToMany(mappedBy = "sucursal")
    private List<Mesa> mesas;

    @OneToMany(mappedBy = "sucursal")
    private List<Producto> productos;

    @OneToMany(mappedBy = "sucursal")
    private List<Subcategoria> subcategorias;
}