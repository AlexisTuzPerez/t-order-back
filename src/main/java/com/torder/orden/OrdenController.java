package com.torder.orden;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torder.orden.dto.CambiarStatusRequest;
import com.torder.orden.dto.CrearOrdenRequest;
import com.torder.orden.dto.OrdenResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {
    private final OrdenRepository ordenRepository;
    private final OrdenService ordenService;

    @Autowired
    public OrdenController(OrdenRepository ordenRepository, OrdenService ordenService) {
        this.ordenRepository = ordenRepository;
        this.ordenService = ordenService;
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<OrdenResponse>> getOrdenesPendientes() {
        List<OrdenResponse> ordenesPendientes = ordenService.getOrdenesPendientes();
        return ResponseEntity.ok(ordenesPendientes);
    }

    @GetMapping("/mis-ordenes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrdenResponse>> getMisOrdenes(Authentication authentication) {
        String username = authentication.getName();
        List<OrdenResponse> misOrdenes = ordenService.getOrdenesPorUsuario(username);
        return ResponseEntity.ok(misOrdenes);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrdenResponse> crearOrden(
            @Valid @RequestBody CrearOrdenRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        OrdenResponse ordenCreada = ordenService.crearOrden(request, username);
        return ResponseEntity.ok(ordenCreada);
    }

    @PutMapping("/{ordenId}/status")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<OrdenResponse> cambiarStatusOrden(
            @PathVariable Long ordenId,
            @Valid @RequestBody CambiarStatusRequest request) {
        
        OrdenResponse ordenActualizada = ordenService.cambiarStatusOrden(ordenId, request.getStatus());
        return ResponseEntity.ok(ordenActualizada);
    }
}
