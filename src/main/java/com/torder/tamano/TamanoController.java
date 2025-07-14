package com.torder.tamano;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tamanos")
public class TamanoController {

    private final TamanoService tamanoService;

    @Autowired
    public TamanoController(TamanoService tamanoService) {
        this.tamanoService = tamanoService;
        System.out.println("=== DEBUG: TamanoController initialized ===");
    }

    @GetMapping
    public ResponseEntity<List<TamanoDTO>> getAllTamanos(Pageable pageable) {
        System.out.println("=== DEBUG: Controller getAllTamanos called ===");
        Page<TamanoDTO> page = tamanoService.getAllTamanos(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                )
        );
        System.out.println("DEBUG: Returning " + page.getContent().size() + " tamanos");
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TamanoDTO> getTamanoById(@PathVariable Long id) {
        TamanoDTO tamanoDTO = tamanoService.getTamanoById(id);
        return ResponseEntity.ok(tamanoDTO);
    }

    @PostMapping
    public ResponseEntity<TamanoDTO> createTamano(@RequestBody TamanoDTO tamanoDTO) {
        TamanoDTO createdTamano = tamanoService.createTamano(tamanoDTO);
        return new ResponseEntity<>(createdTamano, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TamanoDTO> updateTamano(@PathVariable Long id, @RequestBody TamanoDTO tamanoDTO) {
        TamanoDTO updatedTamano = tamanoService.updateTamano(id, tamanoDTO);
        return ResponseEntity.ok(updatedTamano);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTamano(@PathVariable Long id) {
        tamanoService.deleteTamano(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAuth() {
        System.out.println("=== DEBUG: Controller testAuth called ===");
        return ResponseEntity.ok("Autenticación funcionando correctamente");
    }
} 