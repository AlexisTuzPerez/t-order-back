package com.torder.negocioCliente;

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
@RequestMapping("/api/negocios")
public class NegocioClienteController {

    private final NegocioClienteService negocioClienteService;

    public NegocioClienteController(NegocioClienteService negocioClienteService) {
        this.negocioClienteService = negocioClienteService;
    }

    @GetMapping
    public ResponseEntity<List<NegocioDTO>> findAll(Pageable pageable) {


        Page<NegocioDTO> page = negocioClienteService.obtenerTodos(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC,"id"))
                )
        );

        return ResponseEntity.ok(page.getContent());

    }

    @GetMapping("/{id}")
    public ResponseEntity<NegocioDTO> findById(@PathVariable Long id) {
        Optional<NegocioDTO> negocio = negocioClienteService.obtenerPorId(id);
        return negocio.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }




    @PostMapping
    public ResponseEntity<Void> createNegocioCliente(
            @RequestBody @Valid NegocioCliente negocioCliente,
            UriComponentsBuilder ucb) {


        
        NegocioCliente negocioCreado = negocioClienteService.crear(negocioCliente);
        URI location = ucb.path("/api/negocios/{id}")
                .buildAndExpand(negocioCreado.getId())
                .toUri();
                
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNegocioCliente(
            @PathVariable Long id,
            @RequestBody @Valid NegocioCliente negocioCliente) {

        Optional<NegocioDTO> negocioExistente = negocioClienteService.obtenerPorId(id);
        if (negocioExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        negocioClienteService.actualizar(negocioCliente, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNegocioCliente(@PathVariable Long id) {
        negocioClienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
