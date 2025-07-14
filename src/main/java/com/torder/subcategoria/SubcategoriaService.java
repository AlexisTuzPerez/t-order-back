package com.torder.subcategoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final SucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @Autowired
    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository, SucursalRepository sucursalRepository, UserRepository userRepository) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.sucursalRepository = sucursalRepository;
        this.userRepository = userRepository;
    }


    //Método usuario


    public Page<SubcategoriaDTO> getSubcategoriasPorSucursal(Pageable pageable, Long sucursalId){

        Page<Subcategoria> subcategorias = subcategoriaRepository.findBySucursalId(sucursalId, pageable);
        return subcategorias.map(this::convertToDto);



    }



    public Page<SubcategoriaDTO> getAllSubcategorias(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Subcategoria> subcategorias = subcategoriaRepository.findAll(pageable);
            return subcategorias.map(this::convertToDto);
        } else if (currentUser.getRole() == Role.SUCURSAL) {
            Page<Subcategoria> subcategorias = subcategoriaRepository.findBySucursalId(currentUser.getSucursal().getId(), pageable);
            return subcategorias.map(this::convertToDto);
        } else if (currentUser.getRole() == Role.OWNER) {
            Page<Subcategoria> subcategorias = subcategoriaRepository.findBySucursalNegocioId(currentUser.getNegocio().getId(), pageable);
            return subcategorias.map(this::convertToDto);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    }

    public SubcategoriaDTO getSubcategoriaById(Long id) {
        User currentUser = getCurrentUser();
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));
        tieneAccesoSubcategoria(currentUser, subcategoria);
        return convertToDto(subcategoria);
    }

    public SubcategoriaDTO createSubcategoria(SubcategoriaDTO subcategoriaDTO) {
        User currentUser = getCurrentUser();
        
        // Validar que el nombre no esté vacío
        if (subcategoriaDTO.getNombre() == null || subcategoriaDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la subcategoría es obligatorio.");
        }
        
        // Normalizar el nombre a mayúsculas
        String nombreNormalizado = subcategoriaDTO.getNombre().trim().toUpperCase();
        subcategoriaDTO.setNombre(nombreNormalizado);
        
        Subcategoria subcategoria = convertToEntity(subcategoriaDTO);
        tieneAccesoSubcategoria(currentUser, subcategoria);
        subcategoria = subcategoriaRepository.save(subcategoria);
        return convertToDto(subcategoria);
    }

    public SubcategoriaDTO updateSubcategoria(Long id, SubcategoriaDTO subcategoriaDTO) {
        User currentUser = getCurrentUser();
        Subcategoria existingSubcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));

        tieneAccesoSubcategoria(currentUser, existingSubcategoria);

        // Validar que el nombre no esté vacío
        if (subcategoriaDTO.getNombre() == null || subcategoriaDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la subcategoría es obligatorio.");
        }
        
        // Normalizar el nombre a mayúsculas
        String nombreNormalizado = subcategoriaDTO.getNombre().trim().toUpperCase();
        existingSubcategoria.setNombre(nombreNormalizado);

        if (subcategoriaDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(subcategoriaDTO.getSucursalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found"));
            existingSubcategoria.setSucursal(sucursal);
        }

        Subcategoria updatedSubcategoria = subcategoriaRepository.save(existingSubcategoria);
        return convertToDto(updatedSubcategoria);
    }

    public void deleteSubcategoria(Long id) {
        User currentUser = getCurrentUser();
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoria not found"));
        tieneAccesoSubcategoria(currentUser, subcategoria);
        subcategoriaRepository.deleteById(id);
    }

    //Métodos auxiliares



    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private void tieneAccesoSubcategoria(User currentUser, Subcategoria subcategoria) {
        if (currentUser.getRole() == Role.ADMIN ||
                (currentUser.getRole() == Role.SUCURSAL && currentUser.getSucursal().getId().equals(subcategoria.getSucursal().getId())) ||
                (currentUser.getRole() == Role.OWNER && currentUser.getNegocio().getId().equals(subcategoria.getSucursal().getNegocio().getId()))) {
            // Acceso permitido
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
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