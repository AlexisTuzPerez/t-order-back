package com.torder.subcategoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;

@Service
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final SucursalRepository sucursalRepository;

    @Autowired
    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository, SucursalRepository sucursalRepository) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public Page<SubcategoriaDTO> getAllSubcategorias(Pageable pageable) {

        Page<Subcategoria> subcategorias = subcategoriaRepository.findAll(pageable);
        return subcategorias.map(this::convertToDto);
    }

    public SubcategoriaDTO getSubcategoriaById(Long id) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));
        return convertToDto(subcategoria);
    }

    public SubcategoriaDTO createSubcategoria(SubcategoriaDTO subcategoriaDTO) {
        Subcategoria subcategoria = convertToEntity(subcategoriaDTO);
        subcategoria = subcategoriaRepository.save(subcategoria);
        return convertToDto(subcategoria);
    }

    public SubcategoriaDTO updateSubcategoria(Long id, SubcategoriaDTO subcategoriaDTO) {
        Subcategoria existingSubcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));

        existingSubcategoria.setNombre(subcategoriaDTO.getNombre());

        if (subcategoriaDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(subcategoriaDTO.getSucursalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found"));
            existingSubcategoria.setSucursal(sucursal);
        }

        Subcategoria updatedSubcategoria = subcategoriaRepository.save(existingSubcategoria);
        return convertToDto(updatedSubcategoria);
    }

    public void deleteSubcategoria(Long id) {
        subcategoriaRepository.deleteById(id);
    }

    private SubcategoriaDTO convertToDto(Subcategoria subcategoria) {
        SubcategoriaDTO subcategoriaDTO = new SubcategoriaDTO();
        subcategoriaDTO.setId(subcategoria.getId());
        subcategoriaDTO.setNombre(subcategoria.getNombre());
        if (subcategoria.getSucursal() != null) {
            subcategoriaDTO.setSucursalId(subcategoria.getSucursal().getId());
            subcategoriaDTO.setSucursalNombre(subcategoria.getSucursal().getNombre());
        }
        return subcategoriaDTO;
    }

    private Subcategoria convertToEntity(SubcategoriaDTO subcategoriaDTO) {
        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setId(subcategoriaDTO.getId());
        subcategoria.setNombre(subcategoriaDTO.getNombre());

        if (subcategoriaDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(subcategoriaDTO.getSucursalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found"));
            subcategoria.setSucursal(sucursal);
        }
        return subcategoria;
    }
}