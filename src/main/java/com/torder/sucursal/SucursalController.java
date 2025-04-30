package com.torder.sucursal;

import com.torder.negocioCliente.NegocioCliente;
import com.torder.negocioCliente.NegocioClienteRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;
    private final NegocioClienteRepository negocioClienteRepository;

    public SucursalController(SucursalService sucursalService, NegocioClienteRepository negocioClienteRepository) {
        this.sucursalService = sucursalService;
        this.negocioClienteRepository = negocioClienteRepository;

    }

    @GetMapping
    public ResponseEntity<List<SucursalDTO>> findAll(Pageable pageable) {
        Page<SucursalDTO> page = sucursalService.obtenerTodos(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC,"id"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalDTO> findById(@PathVariable Long id) {
        Optional<SucursalDTO> sucursal = sucursalService.obtenerPorId(id);
        return sucursal.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createSucursal(
            @RequestBody @Valid Sucursal sucursal,
            UriComponentsBuilder ucb) {


        Optional<NegocioCliente> negocioExistente = negocioClienteRepository.findById(sucursal.getNegocio().getId());
        if (negocioExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Sucursal sucursalCreada = sucursalService.crear(sucursal);
        URI location = ucb.path("/api/sucursales/{id}")
                .buildAndExpand(sucursalCreada.getId())
                .toUri();
                
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSucursal(
            @PathVariable Long id,
            @RequestBody @Valid Sucursal sucursal) {
        
        Optional<SucursalDTO> existing = sucursalService.obtenerPorId(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        sucursalService.actualizar(sucursal, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSucursal(@PathVariable Long id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
