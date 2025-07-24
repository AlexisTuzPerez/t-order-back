package com.torder.modificador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.torder.relaciones.ModificadorSucursal;
import com.torder.relaciones.ModificadorSucursalRepository;
import com.torder.subcategoria.Subcategoria;
import com.torder.subcategoria.SubcategoriaRepository;
import com.torder.sucursal.Sucursal;
import com.torder.sucursal.SucursalRepository;
import com.torder.user.Role;
import com.torder.user.User;
import com.torder.user.UserRepository;

@Service
@Transactional
public class ModificadorService {

    private final ModificadorRepository modificadorRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final UserRepository userRepository;
    private final SucursalRepository sucursalRepository;
    private final ModificadorSucursalRepository modificadorSucursalRepository;

    @Autowired
    public ModificadorService(ModificadorRepository modificadorRepository, 
                             SubcategoriaRepository subcategoriaRepository, 
                             UserRepository userRepository,
                             SucursalRepository sucursalRepository,
                             ModificadorSucursalRepository modificadorSucursalRepository) {
        this.modificadorRepository = modificadorRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.userRepository = userRepository;
        this.sucursalRepository = sucursalRepository;
        this.modificadorSucursalRepository = modificadorSucursalRepository;
    }

    public Page<ModificadorDTO> getAllModificadores(Pageable pageable) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            Page<Modificador> modificadores = modificadorRepository.findAll(pageable);
            return modificadores.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.OWNER) {
            // OWNER puede ver modificadores de todas las sucursales de su negocio
            List<Sucursal> sucursalesNegocio = sucursalRepository.findByNegocioId(currentUser.getNegocio().getId());
            Page<Modificador> modificadores = modificadorRepository.findBySucursalesSucursalIdIn(
                sucursalesNegocio.stream().map(Sucursal::getId).toList(), pageable);
            return modificadores.map(this::convertToDTO);
        } else if (currentUser.getRole() == Role.SUCURSAL) {
            // SUCURSAL solo puede ver modificadores de su propia sucursal
            if (currentUser.getSucursal() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario SUCURSAL debe tener una sucursal asignada.");
            }
            Page<Modificador> modificadores = modificadorRepository.findBySucursalesSucursalId(
                currentUser.getSucursal().getId(), pageable);
            return modificadores.map(this::convertToDTO);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER y SUCURSAL pueden acceder a modificadores.");
        }
    }

    public List<ModificadorDTO> obtenerModificadoresDeSucursal() {
        User currentUser = getCurrentUser();
        if (currentUser.getSucursal() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene sucursal asignada");
        }

        List<Modificador> modificadores = modificadorRepository.findBySucursalId(currentUser.getSucursal().getId());
        return modificadores.stream().map(this::convertToDTO).toList();
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
        
        // Validar que el nombre no esté vacío
        if (modificadorDTO.getNombre() == null || modificadorDTO.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del modificador es obligatorio.");
        }
        
        // Validar que el precio no sea negativo
        if (modificadorDTO.getPrecio() == null || modificadorDTO.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del modificador debe ser mayor o igual a 0.");
        }
        
        // Validar que la subcategoría sea obligatoria
        if (modificadorDTO.getSubcategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La subcategoría es obligatoria.");
        }
        
        // Obtener la subcategoría
        Subcategoria subcategoria = subcategoriaRepository.findById(modificadorDTO.getSubcategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subcategoría not found with id: " + modificadorDTO.getSubcategoriaId()));
        
        // Crear el modificador
        Modificador modificador = new Modificador();
        modificador.setNombre(modificadorDTO.getNombre().trim().toUpperCase());
        modificador.setPrecio(modificadorDTO.getPrecio());
        modificador.setSubcategoria(subcategoria);
        
        // Guardar el modificador primero
        Modificador savedModificador = modificadorRepository.save(modificador);
        
        // Manejar la asignación a sucursales
        if (currentUser.getRole() == Role.SUCURSAL) {
            // Usuario SUCURSAL no puede especificar sucursales, se asigna automáticamente a su sucursal
            if (modificadorDTO.getSucursalesIds() != null && !modificadorDTO.getSucursalesIds().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Usuario SUCURSAL no puede especificar sucursales. El modificador se asignará automáticamente a su sucursal.");
            }
            
            // Asignar automáticamente a la sucursal del usuario
            Sucursal sucursalUsuario = currentUser.getSucursal();
            System.out.println("Usuario SUCURSAL - Asignando modificador automáticamente a su sucursal: " + sucursalUsuario.getId() + " - " + sucursalUsuario.getNombre());
            
            ModificadorSucursal modificadorSucursal = new ModificadorSucursal();
            modificadorSucursal.setModificador(savedModificador);
            modificadorSucursal.setSucursal(sucursalUsuario);
            modificadorSucursal.setActivo(true);
            
            // Guardar la relación explícitamente
            ModificadorSucursal savedModificadorSucursal = modificadorSucursalRepository.save(modificadorSucursal);
            savedModificador.getSucursales().add(savedModificadorSucursal);
        } else {
            // ADMIN y OWNER pueden especificar sucursales
            if (modificadorDTO.getSucursalesIds() != null && !modificadorDTO.getSucursalesIds().isEmpty()) {
                // Procesar sucursales específicas
                for (Long sucursalId : modificadorDTO.getSucursalesIds()) {
                    Sucursal sucursal = sucursalRepository.findById(sucursalId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal not found with id: " + sucursalId));
                    
                    // Validación para OWNER (solo puede asignar a sucursales de su negocio)
                    if (currentUser.getRole() == Role.OWNER && !sucursal.getNegocio().getId().equals(currentUser.getNegocio().getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar modificadores a sucursales de otros negocios.");
                    }
                    
                    ModificadorSucursal modificadorSucursal = new ModificadorSucursal();
                    modificadorSucursal.setModificador(savedModificador);
                    modificadorSucursal.setSucursal(sucursal);
                    modificadorSucursal.setActivo(true);
                    
                    // Guardar la relación explícitamente
                    ModificadorSucursal savedModificadorSucursal = modificadorSucursalRepository.save(modificadorSucursal);
                    savedModificador.getSucursales().add(savedModificadorSucursal);
                }
            } else {
                // No se especificaron sucursales, asignar a todas las sucursales del negocio
                Long negocioId = currentUser.getRole() == Role.ADMIN ? 
                    subcategoria.getSucursales().stream()
                        .filter(ss -> ss.getActivo())
                        .map(ss -> ss.getSucursal().getNegocio().getId())
                        .findFirst()
                        .orElse(currentUser.getNegocio().getId()) : 
                    currentUser.getNegocio().getId();
                List<Sucursal> sucursalesNegocio = sucursalRepository.findByNegocioId(negocioId);
                
                System.out.println("Usuario " + currentUser.getRole() + " - Asignando modificador a todas las sucursales del negocio " + negocioId + " (" + sucursalesNegocio.size() + " sucursales)");
                
                for (Sucursal sucursal : sucursalesNegocio) {
                    System.out.println("Asignando modificador a sucursal: " + sucursal.getId() + " - " + sucursal.getNombre());
                    ModificadorSucursal modificadorSucursal = new ModificadorSucursal();
                    modificadorSucursal.setModificador(savedModificador);
                    modificadorSucursal.setSucursal(sucursal);
                    modificadorSucursal.setActivo(true);
                    
                    // Guardar la relación explícitamente
                    ModificadorSucursal savedModificadorSucursal = modificadorSucursalRepository.save(modificadorSucursal);
                    savedModificador.getSucursales().add(savedModificadorSucursal);
                }
            }
        }
        
        try {
            return convertToDTO(savedModificador);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Error al crear el modificador. Verifique los datos.");
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
        
        existingModificador.setNombre(nombreNormalizado); // Se convierte a mayúsculas automáticamente
        existingModificador.setPrecio(modificadorDTO.getPrecio());

        try {
            return convertToDTO(modificadorRepository.save(existingModificador));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Error al actualizar el modificador. Verifique los datos.");
        }
    }

    public void deleteModificador(Long id) {
        User currentUser = getCurrentUser();
        Modificador modificador = modificadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modificador not found with id: " + id));

        tieneAccesoModificador(currentUser, modificador);
        
        // Eliminar físicamente las relaciones con sucursales
        modificador.getSucursales().stream()
                .filter(ms -> ms.getSucursal().getId().equals(currentUser.getSucursal().getId()))
                .forEach(ms -> modificadorSucursalRepository.delete(ms));

        // Eliminar físicamente el modificador
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
            modificadorDTO.setSubcategoriaId(modificador.getSubcategoria().getId());
            modificadorDTO.setSubcategoriaNombre(modificador.getSubcategoria().getNombre());
        }
        
        // Cargar sucursales desde la base de datos
        if (modificador.getSucursales() != null && !modificador.getSucursales().isEmpty()) {
            List<Long> sucursalesIds = modificador.getSucursales().stream()
                    .map(ms -> ms.getSucursal().getId())
                    .distinct()
                    .toList();
            modificadorDTO.setSucursalesIds(sucursalesIds);
        }
        
        return modificadorDTO;
    }

    private void tieneAccesoModificador(User currentUser, Modificador modificador) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // ADMIN puede acceder a cualquier modificador
        } else if (currentUser.getRole() == Role.OWNER) {
            // OWNER puede acceder a modificadores de su negocio
            if (modificador.getSucursales() != null && 
                modificador.getSucursales().stream()
                    .anyMatch(ms -> ms.getSucursal().getNegocio().getId().equals(currentUser.getNegocio().getId()))) {
                return;
            }
        } else if (currentUser.getRole() == Role.SUCURSAL) {
            // SUCURSAL puede acceder solo a modificadores de su sucursal
            if (modificador.getSucursales() != null && 
                modificador.getSucursales().stream()
                    .anyMatch(ms -> ms.getSucursal().getId().equals(currentUser.getSucursal().getId()))) {
                return;
            }
        }
        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo ADMIN, OWNER de su negocio y SUCURSAL de su sucursal pueden acceder.");
    }
} 