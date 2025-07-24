package com.torder.subcategoria;

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
@RequestMapping("/api/subcategorias")
@CrossOrigin(origins = "*")
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    @Autowired
    public SubcategoriaController(SubcategoriaService subcategoriaService) {
        this.subcategoriaService = subcategoriaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<SubcategoriaDTO>> obtenerSubcategoriasDeSucursal() {
        try {
            List<SubcategoriaDTO> subcategorias = subcategoriaService.obtenerSubcategoriasDeSucursal();
            return ResponseEntity.ok(subcategorias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<SubcategoriaDTO> obtenerPorId(@PathVariable Long id) {
        try {
            SubcategoriaDTO subcategoria = subcategoriaService.obtenerPorId(id);
            return ResponseEntity.ok(subcategoria);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<SubcategoriaDTO> crear(@Valid @RequestBody Subcategoria subcategoria) {
        try {
            SubcategoriaDTO subcategoriaCreada = subcategoriaService.crear(subcategoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(subcategoriaCreada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<SubcategoriaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody Subcategoria subcategoria) {
        try {
            SubcategoriaDTO subcategoriaActualizada = subcategoriaService.actualizar(id, subcategoria);
            return ResponseEntity.ok(subcategoriaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            subcategoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
} 