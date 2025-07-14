package com.torder.descuento;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.torder.negocioCliente.NegocioCliente;
import com.torder.producto.Producto;
import com.torder.relaciones.DescuentoSucursal;
import com.torder.subcategoria.Subcategoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "descuentos")
@Data
public class Descuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDescuento tipo;

    @Column(nullable = false)
    private Double valor; // Porcentaje, cantidad fija, o cantidad de items gratis

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "monto_minimo")
    private Double montoMinimo; // Mínimo para aplicar descuento

    @Column(name = "monto_maximo")
    private Double montoMaximo; // Máximo descuento aplicable

  

    @Column(name = "usos_maximos")
    private Integer usosMaximos; // Número máximo de veces que se puede usar (null si es descuento automático)

    @Column(name = "usos_actuales")
    private Integer usosActuales = 0; // Veces que ya se ha usado

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private NegocioCliente negocio;

    // Relación con sucursales
    @OneToMany(mappedBy = "descuento")
    private Set<DescuentoSucursal> sucursales = new HashSet<>();

    // Relaciones opcionales para descuentos específicos
    @ManyToMany
    @JoinTable(
        name = "descuento_productos",
        joinColumns = @JoinColumn(name = "descuento_id"),
        inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private Set<Producto> productos = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "descuento_subcategorias",
        joinColumns = @JoinColumn(name = "descuento_id"),
        inverseJoinColumns = @JoinColumn(name = "subcategoria_id")
    )
    private Set<Subcategoria> subcategorias = new HashSet<>();

    // Para combos: productos requeridos para el combo
    @ManyToMany
    @JoinTable(
        name = "descuento_combo_productos",
        joinColumns = @JoinColumn(name = "descuento_id"),
        inverseJoinColumns = @JoinColumn(name = "producto_id")
    )


    private Set<Producto> productosCombo = new HashSet<>();

      // Métodos de ayuda
      public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return activo && 
               (fechaInicio == null || ahora.isAfter(fechaInicio)) &&
               (fechaFin == null || ahora.isBefore(fechaFin));
    }



    
}

/*
 * EJEMPLOS DE USO DE LAS RELACIONES MANY-TO-MANY:
 * 
 * DESCUENTOS AUTOMÁTICOS (sin código):
 * 
 * 1. DESCUENTO POR PRODUCTOS ESPECÍFICOS EN SUCURSAL:
 *    // "20% de descuento en pizzas margherita y pepperoni en sucursal Centro"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.PORCENTAJE);
 *    descuento.setValor(20.0);
 *    descuento.setProductos(Set.of(pizzaMargherita, pizzaPepperoni));
 *    // Agregar a sucursal específica
 *    DescuentoSucursal ds = new DescuentoSucursal();
 *    ds.setDescuento(descuento);
 *    ds.setSucursal(sucursalCentro);
 *    // codigo = null, usosMaximos = null (descuento automático)
 * 
 * 2. DESCUENTO POR SUBCATEGORÍA EN MÚLTIPLES SUCURSALES:
 *    // "30% de descuento en todas las bebidas en sucursales Norte y Sur"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.PORCENTAJE);
 *    descuento.setValor(30.0);
 *    descuento.setSubcategorias(Set.of(subcategoriaBebidas));
 *    // Agregar a múltiples sucursales
 *    descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursalNorte));
 *    descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursalSur));
 * 
 * 3. DESCUENTO 2X1 ESPECÍFICO DE SUCURSAL:
 *    // "2x1 en hamburguesas solo en sucursal Centro"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.DOS_X_UNO);
 *    descuento.setProductos(Set.of(hamburguesa));
 *    descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursalCentro));
 * 
 * 4. DESCUENTO 3X2 EN TODAS LAS SUCURSALES:
 *    // "3x2 en bebidas en todas las sucursales"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.TRES_X_DOS);
 *    descuento.setProductos(Set.of(cocaCola, sprite, fanta));
 *    // Agregar a todas las sucursales del negocio
 *    for (Sucursal sucursal : negocio.getSucursales()) {
 *        descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursal));
 *    }
 * 
 * 5. COMBO ESPECIAL POR SUCURSAL:
 *    // "Combo hamburguesa + papas + bebida por $15 solo en sucursal Norte"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.COMBO);
 *    descuento.setValor(15.0); // Precio del combo
 *    descuento.setProductosCombo(Set.of(hamburguesa, papas, bebida));
 *    descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursalNorte));
 * 
 * 6. BEBIDA GRATIS EN SUCURSAL ESPECÍFICA:
 *    // "Bebida gratis con cualquier hamburguesa en sucursal Sur"
 *    Descuento descuento = new Descuento();
 *    descuento.setTipo(TipoDescuento.BEBIDA_GRATIS);
 *    descuento.setProductos(Set.of(hamburguesa)); // Producto que activa la promoción
 *    descuento.setProductosCombo(Set.of(cocaCola, sprite, fanta)); // Bebidas elegibles
 *    descuento.getSucursales().add(new DescuentoSucursal(descuento, sucursalSur));
 * 
 * CUPONES (con código y límite de usos):
 * 
 * 7. CUPÓN PORCENTAJE PARA SUCURSAL ESPECÍFICA:
 *    // "Cupón WELCOME20: 20% de descuento solo en sucursal Centro"
 *    Descuento cupon = new Descuento();
 *    cupon.setTipo(TipoDescuento.PORCENTAJE);
 *    cupon.setValor(20.0);
 *    cupon.setCodigo("WELCOME20");
 *    cupon.setUsosMaximos(100); // Máximo 100 usos

 *    cupon.getSucursales().add(new DescuentoSucursal(cupon, sucursalCentro));
 * 
 * 8. CUPÓN CANTIDAD FIJA PARA MÚLTIPLES SUCURSALES:
 *    // "Cupón FIRSTORDER: $5 de descuento en sucursales Norte y Sur"
 *    Descuento cupon = new Descuento();
 *    cupon.setTipo(TipoDescuento.CANTIDAD_FIJA);
 *    cupon.setValor(5.0);
 *    cupon.setCodigo("FIRSTORDER");
 *    cupon.setUsosMaximos(50);
 *    cupon.setMostrarEnPantalla(true);
 *    cupon.getSucursales().add(new DescuentoSucursal(cupon, sucursalNorte));
 *    cupon.getSucursales().add(new DescuentoSucursal(cupon, sucursalSur));
 * 
 * 9. CUPÓN 2X1 PARA TODAS LAS SUCURSALES:
 *    // "Cupón BURGER2X1: 2x1 en hamburguesas en todas las sucursales"
 *    Descuento cupon = new Descuento();
 *    cupon.setTipo(TipoDescuento.DOS_X_UNO);
 *    cupon.setProductos(Set.of(hamburguesa));
 *    cupon.setCodigo("BURGER2X1");
 *    cupon.setUsosMaximos(25);
 *    // Agregar a todas las sucursales
 *    for (Sucursal sucursal : negocio.getSucursales()) {
 *        cupon.getSucursales().add(new DescuentoSucursal(cupon, sucursal));
 *    }
 * 
 * LÓGICA DE APLICACIÓN EN EL SERVICIO:
 * 
 * - Para descuentos automáticos: aplicar automáticamente si la orden cumple condiciones Y es de la sucursal correcta
 * - Para cupones: aplicar solo si se ingresa el código, está vigente Y es válido para la sucursal
 * - Para productos específicos: verificar si la orden contiene productos de la lista
 * - Para subcategorías: verificar si la orden contiene productos de esas subcategorías
 * - Para combos: verificar si la orden contiene TODOS los productos del combo
 * - Para 2x1/3x2: contar productos elegibles y aplicar la promoción
 * - Para bebida gratis: buscar la bebida más barata de las elegibles
 * - IMPORTANTE: Siempre verificar que el descuento esté activo en la sucursal de la orden
 */ 