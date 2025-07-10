package com.torder.mesa;

import org.springframework.beans.factory.annotation.Autowired;
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

    //Métodos Sucursal u Owner



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

        Mesa mesa = convertToEntity(mesaDTO);

        tieneAccesoMesa(currentUser, mesa);
        return convertToDTO(mesaRepository.save(mesa));

    }

    public MesaDTO updateMesa(Long id, MesaDTO mesaDTO) {
        User currentUser = getCurrentUser();
        Mesa existingMesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));


        existingMesa.setNumero(mesaDTO.getNumero());
        existingMesa.setSucursal(sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId())));


        tieneAccesoMesa(currentUser, existingMesa);

        return convertToDTO(mesaRepository.save(existingMesa));

    }

    public void deleteMesa(Long id) {
        User currentUser = getCurrentUser();
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));



        tieneAccesoMesa(currentUser, mesa);

        mesaRepository.deleteById(id);

    }






    //Métodos auxiliares


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