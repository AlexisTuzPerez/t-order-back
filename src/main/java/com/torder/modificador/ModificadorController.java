package com.torder.modificador;

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

@RestController
@RequestMapping("/api/modificadores")
@CrossOrigin(origins = "*")
public class ModificadorController {

    private final ModificadorService modificadorService;

    @Autowired
    public ModificadorController(ModificadorService modificadorService) {
        this.modificadorService = modificadorService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<ModificadorDTO>> obtenerModificadoresDeSucursal() {
        try {
            List<ModificadorDTO> modificadores = modificadorService.obtenerModificadoresDeSucursal();
            return ResponseEntity.ok(modificadores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModificadorDTO> getModificadorById(@PathVariable Long id) {
        ModificadorDTO modificadorDTO = modificadorService.getModificadorById(id);
        return ResponseEntity.ok(modificadorDTO);
    }

    @PostMapping
    public ResponseEntity<ModificadorDTO> createModificador(@RequestBody ModificadorDTO modificadorDTO) {
        ModificadorDTO createdModificador = modificadorService.createModificador(modificadorDTO);
        return new ResponseEntity<>(createdModificador, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModificadorDTO> updateModificador(@PathVariable Long id, @RequestBody ModificadorDTO modificadorDTO) {
        ModificadorDTO updatedModificador = modificadorService.updateModificador(id, modificadorDTO);
        return ResponseEntity.ok(updatedModificador);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModificador(@PathVariable Long id) {
        modificadorService.deleteModificador(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Autenticación funcionando correctamente");
    }
} 