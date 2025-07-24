package com.torder.tamano;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tamanos")
@CrossOrigin(origins = "*")
public class TamanoController {

    private final TamanoService tamanoService;

    @Autowired
    public TamanoController(TamanoService tamanoService) {
        this.tamanoService = tamanoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<TamanoDTO>> obtenerTamanosDeSucursal() {
        try {
            List<TamanoDTO> tamanos = tamanoService.obtenerTamanosDeSucursal();
            return ResponseEntity.ok(tamanos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<TamanoDTO> obtenerPorId(@PathVariable Long id) {
        try {
            TamanoDTO tamano = tamanoService.obtenerPorId(id);
            return ResponseEntity.ok(tamano);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<TamanoDTO> crear(@Valid @RequestBody Tamano tamano) {
        try {
            TamanoDTO tamanoCreado = tamanoService.crear(tamano);
            return ResponseEntity.status(HttpStatus.CREATED).body(tamanoCreado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<TamanoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody Tamano tamano) {
        try {
            TamanoDTO tamanoActualizado = tamanoService.actualizar(id, tamano);
            return ResponseEntity.ok(tamanoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            tamanoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
} 