package com.torder.sucursal;

import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @Autowired
    public SucursalService(SucursalRepository sucursalRepository, UserRepository userRepository) {
        this.sucursalRepository = sucursalRepository;
        this.userRepository = userRepository;
    }

    private void validarAccesoAdministrador( ) {

        String email = getCurrentUserEmail();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (usuario.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol de administrador");
        }
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Page<SucursalDTO> obtenerTodos(Pageable pageable) {
        validarAccesoAdministrador();
        Page<Sucursal> sucursales=  sucursalRepository.findAll(pageable);
        return sucursales.map(this::convertirADTO);
    }

    public Optional<SucursalDTO> obtenerPorId(Long id) {
        validarAccesoAdministrador();
        Sucursal sucursal = sucursalRepository.findById(id).get();
        return  Optional.of(convertirADTO(sucursal));
    }

    public Sucursal crear(Sucursal sucursal) {
        validarAccesoAdministrador();
        return sucursalRepository.save(sucursal);
    }

    public Sucursal actualizar(Sucursal sucursal, Long id) {
        validarAccesoAdministrador();
        Optional<Sucursal> sucursalExistente = sucursalRepository.findById(id);

        sucursalExistente.get().setNombre(sucursal.getNombre());
        sucursalExistente.get().setCuidad(sucursal.getCuidad());
        sucursalExistente.get().setEstado(sucursal.getEstado());
        return sucursalRepository.save(sucursalExistente.get());
    }

    public void eliminar(Long id) {
        validarAccesoAdministrador();
        sucursalRepository.deleteById(id);
    }

    private SucursalDTO convertirADTO(Sucursal sucursal) {
        SucursalDTO dto = new SucursalDTO();
        dto.setId(sucursal.getId());
        dto.setNombre(sucursal.getNombre());
        dto.setCuidad(sucursal.getCuidad());
        dto.setEstado(sucursal.getEstado());
        dto.setNegocioId(sucursal.getNegocio().getId()); // Solo el ID
        return dto;
    }
}
