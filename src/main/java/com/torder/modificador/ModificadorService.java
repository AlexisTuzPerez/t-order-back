package com.torder.modificador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.sucursal.Sucursal;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class ModificadorService {

    private final ModificadorRepository modificadorRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final UserRepository userRepository;

    @Autowired
    public ModificadorService(ModificadorRepository modificadorRepository, SubcategoriaRepository subcategoriaRepository, UserRepository userRepository) {
        this.modificadorRepository = modificadorRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.userRepository = userRepository;
    }

    public Page<ModificadorDTO> getAllModificadores(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Modificador> modificadores = modificadorRepository.findAll(pageable);
            return modificadores.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.OWNER && currentUser.getSucursal() != null) {
            Page<Modificador> modificadores = modificadorRepository.findBySubcategoriaSucursal(currentUser.getSucursal(), pageable);
            return modificadores.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.SUCURSAL && currentUser.getSucursal() != null) {
            try {
                Page<Modificador> modificadores = modificadorRepository.findBySubcategoriaSucursal(currentUser.getSucursal(), pageable);
                return modificadores.map(this::convertToDTO);
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden acceder a modificadores.");
        }
    }

    public ModificadorDTO getModificadorById(Long id) {
        User currentUser = getCurrentUser();
        
        Modificador modificador = modificadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modificador not found with id: " + id));

        tieneAccesoModificador(currentUser, modificador);
        return convertToDTO(modificador);
    }

    public ModificadorDTO createModificador(ModificadorDTO modificadorDTO) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.SUCURSAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden crear modificadores.");
        }
        
        if ((currentUser.getRole() == Role.OWNER || currentUser.getRole() == Role.SUCURSAL) && currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario " + currentUser.getRole() + " debe tener una sucursal asignada.");
        }
        
        // Validar que el nombre no esté vacío
        if (modificadorDTO.getNombre() == null || modificadorDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del modificador es obligatorio.");
        }
        
        // Validar que el precio no sea negativo
        if (modificadorDTO.getPrecio() == null || modificadorDTO.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del modificador debe ser mayor o igual a 0.");
        }
        
        // Obtener la sucursal del usuario autenticado
        Sucursal sucursal = currentUser.getSucursal();
        if (sucursal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no tiene sucursal asignada.");
        }
        
        // Crear una subcategoría temporal o usar una existente
        // Por ahora, vamos a crear un modificador sin subcategoría específica
        // TODO: Implementar lógica para seleccionar subcategoría
        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setId(1L); // Temporal - necesitamos implementar la lógica correcta
        subcategoria.setNombre("General");
        subcategoria.setSucursal(sucursal);
        
        // Verificar si ya existe un modificador con el mismo nombre
        String nombreNormalizado = modificadorDTO.getNombre().trim().toUpperCase();
        if (modificadorRepository.existsByNombre(nombreNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un modificador con el nombre '" + nombreNormalizado + "'.");
        }
        
        Modificador modificador = new Modificador();
        modificador.setNombre(nombreNormalizado); // Se convierte a mayúsculas automáticamente
        modificador.setPrecio(modificadorDTO.getPrecio());
        modificador.setSubcategoria(subcategoria);
        
        try {
            Modificador savedModificador = modificadorRepository.save(modificador);
            return convertToDTO(savedModificador);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un modificador con el nombre '" + nombreNormalizado + "'.");
        }
    }

    public ModificadorDTO updateModificador(Long id, ModificadorDTO modificadorDTO) {
        User currentUser = getCurrentUser();
        
        Modificador existingModificador = modificadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modificador not found with id: " + id));

        tieneAccesoModificador(currentUser, existingModificador);

        // Validar que el nombre no esté vacío
        if (modificadorDTO.getNombre() == null || modificadorDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del modificador es obligatorio.");
        }
        
        // Validar que el precio no sea negativo
        if (modificadorDTO.getPrecio() == null || modificadorDTO.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del modificador debe ser mayor o igual a 0.");
        }
        
        // Normalizar el nombre a mayúsculas
        String nombreNormalizado = modificadorDTO.getNombre().trim().toUpperCase();
        
        // Verificar si ya existe otro modificador con el mismo nombre (excluyendo el actual)
        if (modificadorRepository.existsByNombreAndIdNot(nombreNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un modificador con el nombre '" + nombreNormalizado + "'.");
        }

        existingModificador.setNombre(nombreNormalizado); // Se convierte a mayúsculas automáticamente
        existingModificador.setPrecio(modificadorDTO.getPrecio());

        try {
            return convertToDTO(modificadorRepository.save(existingModificador));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un modificador con el nombre '" + nombreNormalizado + "'.");
        }
    }

    public void deleteModificador(Long id) {
        User currentUser = getCurrentUser();
        Modificador modificador = modificadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modificador not found with id: " + id));

        tieneAccesoModificador(currentUser, modificador);
        modificadorRepository.deleteById(id);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (email == null || email.equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return user;
    }

    private ModificadorDTO convertToDTO(Modificador modificador) {
        ModificadorDTO modificadorDTO = new ModificadorDTO();
        modificadorDTO.setId(modificador.getId());
        modificadorDTO.setNombre(modificador.getNombre());
        modificadorDTO.setPrecio(modificador.getPrecio());
        if (modificador.getSubcategoria() != null) {
            modificadorDTO.setSubcategoriaNombre(modificador.getSubcategoria().getNombre());
        }
        return modificadorDTO;
    }

    private void tieneAccesoModificador(User currentUser, Modificador modificador) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // ADMIN puede acceder a cualquier modificador
        } else if ((currentUser.getRole() == Role.OWNER || currentUser.getRole() == Role.SUCURSAL) && 
                   currentUser.getSucursal() != null && 
                   modificador.getSubcategoria() != null &&
                   modificador.getSubcategoria().getSucursal() != null &&
                   currentUser.getSucursal().getId().equals(modificador.getSubcategoria().getSucursal().getId())) {
            return; // OWNER y SUCURSAL pueden acceder solo a modificadores de su sucursal
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL de la sucursal correspondiente pueden acceder.");
        }
    }
} 