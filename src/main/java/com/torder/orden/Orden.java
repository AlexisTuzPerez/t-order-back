package com.torder.orden;

import com.torder.mesa.Mesa;
import com.torder.ordenProducto.OrdenProducto;
import com.torder.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


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

    private Double total;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @OneToMany(mappedBy = "orden")
    private List<OrdenProducto> items;
}

