package com.torder.orden;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torder.mesa.Mesa;
import com.torder.mesa.MesaRepository;
import com.torder.modificador.Modificador;
import com.torder.modificador.ModificadorRepository;
import com.torder.orden.dto.CrearOrdenRequest;
import com.torder.orden.dto.OrdenResponse;
import com.torder.producto.Producto;
import com.torder.producto.ProductoRepository;
import com.torder.relaciones.OrdenProducto;
import com.torder.relaciones.OrdenProductoModificador;
import com.torder.relaciones.ProductoTamano;
import com.torder.tamano.ProductoTamanoRepository;
import com.torder.tamano.Tamano;
import com.torder.tamano.TamanoRepository;
import com.torder.user.User;
import com.torder.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final TamanoRepository tamanoRepository;
    private final ModificadorRepository modificadorRepository;
    private final MesaRepository mesaRepository;
    private final UserRepository userRepository;
    private final ProductoTamanoRepository productoTamanoRepository;

    @Transactional
    public OrdenResponse crearOrden(CrearOrdenRequest request, String username) {
        // Buscar usuario
        User usuario = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar mesa
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // Crear orden
        Orden orden = Orden.builder()
                .usuario(usuario)
                .mesa(mesa)
                .notas(request.getNotas())
                .estado(Status.PENDIENTE)
                .items(new ArrayList<>())
                .build();

        double totalOrden = 0.0;

        // Procesar cada producto
        for (CrearOrdenRequest.ProductoOrdenRequest productoRequest : request.getProductos()) {
            // Buscar producto
            Producto producto = productoRepository.findById(productoRequest.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoRequest.getProductoId()));

            // Buscar tamaño
            Tamano tamano = tamanoRepository.findById(productoRequest.getTamanoId())
                    .orElseThrow(() -> new RuntimeException("Tamaño no encontrado: " + productoRequest.getTamanoId()));

            // Buscar precio del producto con el tamaño específico
            ProductoTamano productoTamano = productoTamanoRepository.findByProductoIdAndTamanoId(producto.getId(), tamano.getId());
            if (productoTamano == null) {
                throw new RuntimeException("Precio no encontrado para producto " + producto.getNombre() + " con tamaño " + tamano.getNombre());
            }

            // Crear OrdenProducto
            OrdenProducto ordenProducto = OrdenProducto.builder()
                    .orden(orden)
                    .producto(producto)
                    .tamano(tamano)
                    .precio(productoTamano.getPrecio())
                    .cantidad(productoRequest.getCantidad())
                    .modificadores(new ArrayList<>())
                    .build();

            double subtotalProducto = productoTamano.getPrecio() * productoRequest.getCantidad();

            // Procesar modificadores si existen
            if (productoRequest.getModificadores() != null) {
                // Agrupar modificadores por ID y contar repeticiones
                Map<Long, Long> modificadoresCount = productoRequest.getModificadores().stream()
                        .collect(Collectors.groupingBy(
                                CrearOrdenRequest.ModificadorOrdenRequest::getModificadorId,
                                Collectors.counting()
                        ));

                for (Map.Entry<Long, Long> entry : modificadoresCount.entrySet()) {
                    Long modificadorId = entry.getKey();
                    Long cantidad = entry.getValue();
                    
                    Modificador modificador = modificadorRepository.findById(modificadorId)
                            .orElseThrow(() -> new RuntimeException("Modificador no encontrado: " + modificadorId));

                    OrdenProductoModificador ordenProductoModificador = OrdenProductoModificador.builder()
                            .ordenProducto(ordenProducto)
                            .modificador(modificador)
                            .precioModificador(modificador.getPrecio())
                            .cantidad(cantidad.intValue())
                            .build();

                    ordenProducto.getModificadores().add(ordenProductoModificador);
                    subtotalProducto += modificador.getPrecio() * cantidad;
                }
            }

            orden.getItems().add(ordenProducto);
            totalOrden += subtotalProducto;
        }

        orden.setTotal(totalOrden);

        // Guardar orden
        Orden ordenGuardada = ordenRepository.save(orden);

        return convertirAOrdenResponse(ordenGuardada);
    }

    public List<OrdenResponse> getOrdenesPendientes() {
        List<Orden> ordenesPendientes = ordenRepository.findByEstado(Status.PENDIENTE);
        return ordenesPendientes.stream()
                .map(this::convertirAOrdenResponse)
                .toList();
    }

    public List<OrdenResponse> getOrdenesPorUsuario(String username) {
        User usuario = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Orden> ordenesUsuario = ordenRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
        return ordenesUsuario.stream()
                .map(this::convertirAOrdenResponse)
                .toList();
    }

    @Transactional
    public OrdenResponse cambiarStatusOrden(Long ordenId, Status nuevoEstado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));
        
        orden.setEstado(nuevoEstado);
        Orden ordenActualizada = ordenRepository.save(orden);
        
        return convertirAOrdenResponse(ordenActualizada);
    }

    private OrdenResponse convertirAOrdenResponse(Orden orden) {
        List<OrdenResponse.ProductoOrdenResponse> productosResponse = new ArrayList<>();

        for (OrdenProducto ordenProducto : orden.getItems()) {
            List<OrdenResponse.ModificadorOrdenResponse> modificadoresResponse = new ArrayList<>();

            for (OrdenProductoModificador opm : ordenProducto.getModificadores()) {
                modificadoresResponse.add(OrdenResponse.ModificadorOrdenResponse.builder()
                        .id(opm.getId())
                        .modificadorNombre(opm.getModificador().getNombre())
                        .precioModificador(opm.getPrecioModificador())
                        .cantidad(opm.getCantidad())
                        .subtotal(opm.getPrecioModificador() * opm.getCantidad())
                        .build());
            }

            productosResponse.add(OrdenResponse.ProductoOrdenResponse.builder()
                    .id(ordenProducto.getId())
                    .productoNombre(ordenProducto.getProducto().getNombre())
                    .tamanoNombre(ordenProducto.getTamano().getNombre())
                    .precio(ordenProducto.getPrecio())
                    .cantidad(ordenProducto.getCantidad())
                    .subtotal(ordenProducto.getPrecio() * ordenProducto.getCantidad())
                    .modificadores(modificadoresResponse)
                    .build());
        }

        return OrdenResponse.builder()
                .id(orden.getId())
                .estado(orden.getEstado())
                .notas(orden.getNotas())
                .total(orden.getTotal())
                .fechaCreacion(orden.getFechaCreacion())
                .usuarioNombre(orden.getUsuario().getFirstname() + " " + orden.getUsuario().getLastname())
                .mesaNombre("Mesa " + orden.getMesa().getNumero())
                .productos(productosResponse)
                .build();
    }
} 