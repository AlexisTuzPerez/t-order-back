package com.torder.negocioCliente;

import java.util.Optional;
import java.util.stream.Collectors;

import com.torder.sucursal.Sucursal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class NegocioClienteService {

    private final NegocioClienteRepository repositorioNegocioCliente;
    private final UserRepository repositorioUsuario;

    @Autowired
    public NegocioClienteService(NegocioClienteRepository repositorioNegocioCliente, UserRepository repositorioUsuario) {
        this.repositorioNegocioCliente = repositorioNegocioCliente;
        this.repositorioUsuario = repositorioUsuario;

    }

    private void validarEmail() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

    }


    public Page<NegocioDTO> obtenerTodos(Pageable pageable) {
        validarEmail();


        Page<NegocioCliente> negocios = repositorioNegocioCliente.findAll(pageable);
        return negocios.map(this::convertirADTO);
    }

    public Optional<NegocioDTO> obtenerPorId(Long id) {
        validarEmail();

        NegocioCliente negocioCliente = repositorioNegocioCliente.findById(id).get();

        return Optional.of(convertirADTO(negocioCliente));
    }

    public NegocioCliente crear(NegocioCliente negocioCliente ) {
        validarEmail();
        return repositorioNegocioCliente.save(negocioCliente);
    }

    public NegocioCliente actualizar(NegocioCliente negocioCliente,Long id) {
        validarEmail();

        Optional<NegocioCliente> negocioExistente = repositorioNegocioCliente.findById(id);
        negocioExistente.get().setNombre(negocioCliente.getNombre());
        negocioExistente.get().setTelefono(negocioCliente.getTelefono());
        negocioExistente.get().setMail(negocioCliente.getMail());
        return repositorioNegocioCliente.save(negocioExistente.get());
    }

    public void eliminar(Long id ) {
        validarEmail();

        repositorioNegocioCliente.deleteById(id);
    }

    public NegocioDTO convertirADTO(NegocioCliente negocio) {
        NegocioDTO dto = new NegocioDTO();
        dto.setId(negocio.getId());
        dto.setNombre(negocio.getNombre());
        dto.setTelefono(negocio.getTelefono());
        dto.setMail(negocio.getMail());

        // Convert related entities to IDs
        if (negocio.getSucursales() != null) {
            dto.setSucursalesIds(
                    negocio.getSucursales().stream()
                            .map(Sucursal::getId)
                            .collect(Collectors.toList())
            );
        }

        if (negocio.getUsuarios() != null) {
            dto.setUsuariosIds(
                    negocio.getUsuarios().stream()
                            .map(User::getId)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }



}