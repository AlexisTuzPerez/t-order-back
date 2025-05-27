package com.torder.mesa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.torder.sucursal.SucursalRepository;

@Service
public class MesaService {


    private final MesaRepository mesaRepository;


    private final SucursalRepository sucursalRepository;


    @Autowired
    public MesaService(MesaRepository mesaRepository, SucursalRepository sucursalRepository) {
        this.mesaRepository = mesaRepository;
        this.sucursalRepository = sucursalRepository;
    }



    public Page<MesaDTO> getAllMesas(Pageable pageable) {



        Page<Mesa> mesas = mesaRepository.findAll(pageable);
        return mesas.map(this::convertToDTO);
    }

    public MesaDTO getMesaById(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));
        return convertToDTO(mesa);
    }

    public MesaDTO createMesa(MesaDTO mesaDTO) {
        Mesa mesa = convertToEntity(mesaDTO);
        return convertToDTO(mesaRepository.save(mesa));
    }

    public MesaDTO updateMesa(Long id, MesaDTO mesaDTO) {
        Mesa existingMesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id));

        existingMesa.setNumero(mesaDTO.getNumero());
        existingMesa.setSucursal(sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId())));

        return convertToDTO(mesaRepository.save(existingMesa));
    }

    public void deleteMesa(Long id) {
        if (!mesaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa not found with id: " + id);
        }
        mesaRepository.deleteById(id);
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

    private Mesa convertToEntity(MesaDTO mesaDTO) {
        Mesa mesa = new Mesa();
        mesa.setId(mesaDTO.getId());
        mesa.setNumero(mesaDTO.getNumero());
        mesa.setSucursal(sucursalRepository.findById(mesaDTO.getSucursalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + mesaDTO.getSucursalId())));
        return mesa;
    }
}