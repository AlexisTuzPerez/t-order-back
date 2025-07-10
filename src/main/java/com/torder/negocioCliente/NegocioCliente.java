package com.torder.negocioCliente;

import com.torder.sucursal.Sucursal;
import com.torder.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class NegocioCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del negocio es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 10, max = 10, message = "El teléfono debe tener 10 dígitos")
    @Pattern(regexp = "\\d+", message = "El teléfono solo debe contener números")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String mail;

    @OneToMany(mappedBy = "negocio")
    private List<Sucursal> sucursales;

    @OneToMany(mappedBy = "negocio")
    private List<User> usuarios;


    private Boolean activo;

}