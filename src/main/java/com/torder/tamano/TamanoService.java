package com.torder.tamano;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.negocioCliente.NegocioClienteRepository;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class TamanoService {

    private final TamanoRepository tamanoRepository;
    private final NegocioClienteRepository negocioClienteRepository;
    private final UserRepository userRepository;

    @Autowired
    public TamanoService(TamanoRepository tamanoRepository, NegocioClienteRepository negocioClienteRepository, UserRepository userRepository) {
        this.tamanoRepository = tamanoRepository;
        this.negocioClienteRepository = negocioClienteRepository;
        this.userRepository = userRepository;
    }

    public Page<TamanoDTO> getAllTamanos(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Tamano> tamanos = tamanoRepository.findAll(pageable);
            return tamanos.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.OWNER && currentUser.getNegocio() != null) {
            Page<Tamano> tamanos = tamanoRepository.findByNegocio(currentUser.getNegocio(), pageable);
            return tamanos.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.SUCURSAL && currentUser.getNegocio() != null) {
            try {
                Page<Tamano> tamanos = tamanoRepository.findByNegocio(currentUser.getNegocio(), pageable);
                return tamanos.map(this::convertToDTO);
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden acceder a tamaños.");
        }
    }

    public TamanoDTO getTamanoById(Long id) {
        User currentUser = getCurrentUser();
        
        Tamano tamano = tamanoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + id));

        tieneAccesoTamano(currentUser, tamano);
        return convertToDTO(tamano);
    }

    public TamanoDTO createTamano(TamanoDTO tamanoDTO) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.SUCURSAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden crear tamaños.");
        }
        
        if ((currentUser.getRole() == Role.OWNER || currentUser.getRole() == Role.SUCURSAL) && currentUser.getNegocio() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario " + currentUser.getRole() + " debe tener un negocio asignado.");
        }
        
        // Validar que el nombre no esté vacío
        if (tamanoDTO.getNombre() == null || tamanoDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del tamaño es obligatorio.");
        }
        
        // Verificar si ya existe un tamaño con el mismo nombre en el mismo negocio
        String nombreNormalizado = tamanoDTO.getNombre().trim().toUpperCase();
        if (tamanoRepository.existsByNombreAndNegocio(nombreNormalizado, currentUser.getNegocio())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un tamaño con el nombre '" + nombreNormalizado + "' en este negocio.");
        }
        
        Tamano tamano = new Tamano();
        tamano.setNombre(nombreNormalizado); // Se convierte a mayúsculas automáticamente
        tamano.setDescripcion(tamanoDTO.getDescripcion());
        tamano.setNegocio(currentUser.getNegocio());
        
        try {
            Tamano savedTamano = tamanoRepository.save(tamano);
            return convertToDTO(savedTamano);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un tamaño con el nombre '" + nombreNormalizado + "' en este negocio.");
        }
    }

    public TamanoDTO updateTamano(Long id, TamanoDTO tamanoDTO) {
        User currentUser = getCurrentUser();
        
        Tamano existingTamano = tamanoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + id));

        tieneAccesoTamano(currentUser, existingTamano);

        // Validar que el nombre no esté vacío
        if (tamanoDTO.getNombre() == null || tamanoDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del tamaño es obligatorio.");
        }
        
        // Normalizar el nombre a mayúsculas
        String nombreNormalizado = tamanoDTO.getNombre().trim().toUpperCase();
        
        // Verificar si ya existe otro tamaño con el mismo nombre en el mismo negocio (excluyendo el actual)
        if (tamanoRepository.existsByNombreAndNegocioAndIdNot(nombreNormalizado, existingTamano.getNegocio(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un tamaño con el nombre '" + nombreNormalizado + "' en este negocio.");
        }

        existingTamano.setNombre(nombreNormalizado); // Se convierte a mayúsculas automáticamente
        existingTamano.setDescripcion(tamanoDTO.getDescripcion());

        try {
            return convertToDTO(tamanoRepository.save(existingTamano));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe un tamaño con el nombre '" + nombreNormalizado + "' en este negocio.");
        }
    }

    public void deleteTamano(Long id) {
        User currentUser = getCurrentUser();
        Tamano tamano = tamanoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tamaño not found with id: " + id));

        tieneAccesoTamano(currentUser, tamano);
        tamanoRepository.deleteById(id);
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

    private TamanoDTO convertToDTO(Tamano tamano) {
        TamanoDTO tamanoDTO = new TamanoDTO();
        tamanoDTO.setId(tamano.getId());
        tamanoDTO.setNombre(tamano.getNombre());
        tamanoDTO.setDescripcion(tamano.getDescripcion());
        if (tamano.getNegocio() != null) {
            tamanoDTO.setNegocioNombre(tamano.getNegocio().getNombre());
        }
        return tamanoDTO;
    }

    private void tieneAccesoTamano(User currentUser, Tamano tamano) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // ADMIN puede acceder a cualquier tamaño
        } else if ((currentUser.getRole() == Role.OWNER || currentUser.getRole() == Role.SUCURSAL) && 
                   currentUser.getNegocio() != null && 
                   tamano.getNegocio() != null &&
                   currentUser.getNegocio().getId().equals(tamano.getNegocio().getId())) {
            return; // OWNER y SUCURSAL pueden acceder solo a tamaños de su negocio
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL del negocio correspondiente pueden acceder.");
        }
    }

    private Tamano convertToEntity(TamanoDTO tamanoDTO) {
        Tamano tamano = new Tamano();
        tamano.setId(tamanoDTO.getId());
        tamano.setNombre(tamanoDTO.getNombre());
        tamano.setDescripcion(tamanoDTO.getDescripcion());
        return tamano;
    }
} 