package com.torder.subcategoria;

import com.torder.mesa.MesaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import java.util.List;

@RestController
@RequestMapping("/api/subcategorias")
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    @Autowired
    public SubcategoriaController(SubcategoriaService subcategoriaService) {
        this.subcategoriaService = subcategoriaService;
    }

    @GetMapping
    public ResponseEntity<List<SubcategoriaDTO>> getAllSubcategorias(Pageable pageable) {


        Page<SubcategoriaDTO> page = subcategoriaService.getAllSubcategorias(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC,"id"))
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubcategoriaDTO> getSubcategoriaById(@PathVariable Long id) {
        return ResponseEntity.ok(subcategoriaService.getSubcategoriaById(id));
    }

    @PostMapping
    public ResponseEntity<SubcategoriaDTO> createSubcategoria(@RequestBody SubcategoriaDTO subcategoriaDTO) {
        SubcategoriaDTO createdSubcategoria = subcategoriaService.createSubcategoria(subcategoriaDTO);
        return new ResponseEntity<>(createdSubcategoria, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubcategoriaDTO> updateSubcategoria(@PathVariable Long id, @RequestBody SubcategoriaDTO subcategoriaDTO) {
        SubcategoriaDTO updatedSubcategoria = subcategoriaService.updateSubcategoria(id, subcategoriaDTO);
        return ResponseEntity.ok(updatedSubcategoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubcategoria(@PathVariable Long id) {
        subcategoriaService.deleteSubcategoria(id);
        return ResponseEntity.noContent().build();
    }
}