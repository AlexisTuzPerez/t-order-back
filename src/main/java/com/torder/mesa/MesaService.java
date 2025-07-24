package com.torder.mesa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.sucursal.SucursalRepository;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class MesaService {

    private final MesaRepository mesaRepository;
    private final SucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @Autowired
    public MesaService(MesaRepository mesaRepository, SucursalRepository sucursalRepository, UserRepository userRepository) {
        this.mesaRepository = mesaRepository;
        this.sucursalRepository = sucursalRepository;
        this.userRepository = userRepository;
    }

    public Page<MesaDTO> getAllMesas(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Mesa> mesas = mesaRepository.findAll(pageable);
            return mesas.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.SUCURSAL) {
            Page<Mesa> mesas = mesaRepository.findBySucursalId(currentUser.getSucursal().getId(), pageable);
            return mesas.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.OWNER) {
            Page<Mesa> mesas = mesaRepository.findBySucursalNegocioId(currentUser.getNegocio().getId(), pageable);
            return mesas.map(this::convertToDTO);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    }

    public MesaDTO getMesaById(Long id) {
        User currentUser = getCurrentUser();
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));

        tieneAccesoMesa(currentUser, mesa);
        return convertToDTO(mesa);
    }

    public MesaDTO createMesa(MesaDTO mesaDTO) {
        User currentUser = getCurrentUser();
        
        // Validar que el número de mesa no esté vacío
        if (mesaDTO.getNumero() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de mesa es obligatorio.");
        }
        
        // Validar que la sucursal sea obligatoria
        if (mesaDTO.getSucursalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sucursal es obligatoria.");
        }
        
        // Obtener la sucursal
        com.torder.sucursal.Sucursal sucursal = sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId()));
        
        // Validaciones según el rol del usuario
        if (currentUser.getRole() == Role.SUCURSAL) {
            // Usuario SUCURSAL solo puede crear mesas en su propia sucursal
            if (!sucursal.getId().equals(currentUser.getSucursal().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Usuario SUCURSAL solo puede crear mesas en su propia sucursal (ID: " + currentUser.getSucursal().getId() + ")");
            }
        } else if (currentUser.getRole() == Role.OWNER) {
            // OWNER solo puede crear mesas en sucursales de su negocio
            if (!sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear mesas en sucursales de otros negocios.");
            }
        }
        // ADMIN puede crear mesas en cualquier sucursal
        
        Mesa mesa = new Mesa();
        mesa.setNumero(mesaDTO.getNumero());
        mesa.setSucursal(sucursal);
        
        try {
            return convertToDTO(mesaRepository.save(mesa));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe una mesa con el número " + mesaDTO.getNumero() + " en esta sucursal.");
        }
    }

    public MesaDTO updateMesa(Long id, MesaDTO mesaDTO) {
        User currentUser = getCurrentUser();
        Mesa existingMesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));

        tieneAccesoMesa(currentUser, existingMesa);

        // Validar que el número de mesa no esté vacío
        if (mesaDTO.getNumero() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de mesa es obligatorio.");
        }
        
        // Validar que la sucursal sea obligatoria
        if (mesaDTO.getSucursalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sucursal es obligatoria.");
        }
        
        // Obtener la sucursal
        com.torder.sucursal.Sucursal sucursal = sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId()));
        
        // Validaciones según el rol del usuario
        if (currentUser.getRole() == Role.SUCURSAL) {
            // Usuario SUCURSAL solo puede actualizar mesas en su propia sucursal
            if (!sucursal.getId().equals(currentUser.getSucursal().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Usuario SUCURSAL solo puede actualizar mesas en su propia sucursal (ID: " + currentUser.getSucursal().getId() + ")");
            }
        } else if (currentUser.getRole() == Role.OWNER) {
            // OWNER solo puede actualizar mesas en sucursales de su negocio
            if (!sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes actualizar mesas en sucursales de otros negocios.");
            }
        }
        // ADMIN puede actualizar mesas en cualquier sucursal

        existingMesa.setNumero(mesaDTO.getNumero());
        existingMesa.setSucursal(sucursal);

        try {
            return convertToDTO(mesaRepository.save(existingMesa));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe una mesa con el número " + mesaDTO.getNumero() + " en esta sucursal.");
        }
    }

    public void deleteMesa(Long id) {
        User currentUser = getCurrentUser();
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));

        tieneAccesoMesa(currentUser, mesa);
        mesaRepository.deleteById(id);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private MesaDTO convertToDTO(Mesa mesa) {
        MesaDTO mesaDTO = new MesaDTO();
        mesaDTO.setId(mesa.getId());
        mesaDTO.setNumero(mesa.getNumero());
        if (mesa.getSucursal() != null) {
            mesaDTO.setSucursalId(mesa.getSucursal().getId());
            mesaDTO.setSucursalNombre(mesa.getSucursal().getNombre());
        }
        return mesaDTO;
    }

    private void tieneAccesoMesa(User currentUser, Mesa mesa) {
        if( currentUser.getRole() == Role.ADMIN ||
               (currentUser.getRole() == Role.SUCURSAL && currentUser.getSucursal().getId().equals(mesa.getSucursal().getId())) ||
               (currentUser.getRole() == Role.OWNER && currentUser.getNegocio().getId().equals(mesa.getSucursal().getNegocio().getId()))) {
               } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    }

    private Mesa convertToEntity(MesaDTO mesaDTO) {
        Mesa mesa = new Mesa();
        mesa.setId(mesaDTO.getId());
        mesa.setNumero(mesaDTO.getNumero());
        mesa.setSucursal(sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId())));
        return mesa;
    }
}