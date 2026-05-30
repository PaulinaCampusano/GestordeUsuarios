package com.gestion.usuario.service;

import com.gestion.usuario.dto.AdminUpdateDTO;
import com.gestion.usuario.dto.UserRegisterDTO;
import com.gestion.usuario.entity.*;
import com.gestion.usuario.exception.*;
import com.gestion.usuario.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModuloRepository moduloRepository;
    private final AccesoRepository accesoRepository;
    private final RoleModuloRepository roleModuloRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       ModuloRepository moduloRepository,
                       AccesoRepository accesoRepository,
                       RoleModuloRepository roleModuloRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.moduloRepository = moduloRepository;
        this.accesoRepository = accesoRepository;
        this.roleModuloRepository = roleModuloRepository;
    }

    // ── USUARIOS ─────────────────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Transactional
    public User adminCreateUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("El correo ingresado ya se encuentra registrado");
        }
        if (dto.getRut() != null && !dto.getRut().isBlank()) {
            if (userRepository.existsByRut(dto.getRut())) {
                throw new RutAlreadyExistsException("El RUT ingresado ya se encuentra registrado");
            }
        }

        PasswordPolicy.validate(dto.getPassword());

        User user = new User();
        user.setEmail(dto.getEmail().toLowerCase());
        user.setNombreCompleto(dto.getNombreCompleto());
        user.setRut(dto.getRut());
        user.setTelefono(dto.getTelefono());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setIsActive(true);

        Role role = resolveRole(dto, "ROLE_USER");
        user.setRole(role);

        return userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, AdminUpdateDTO dto) {
        if (dto == null || dto.isEmpty()) {
            throw new NoFieldsToUpdateException(
                    "Debes enviar al menos un campo para actualizar: email, nombreCompleto, rut, telefono o password");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String normalizedEmail = dto.getEmail().toLowerCase();
            if (!user.getEmail().equals(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
                throw new EmailAlreadyExistsException("El nuevo correo ya está en uso");
            }
            user.setEmail(normalizedEmail);
        }

        if (dto.getNombreCompleto() != null && !dto.getNombreCompleto().isBlank()) {
            user.setNombreCompleto(dto.getNombreCompleto());
        }

        if (dto.getRut() != null && !dto.getRut().isBlank()) {
            boolean rutEnUso = userRepository.findAll().stream()
                    .anyMatch(u -> !u.getId().equals(userId) && dto.getRut().equals(u.getRut()));
            if (rutEnUso) {
                throw new RutAlreadyExistsException("El RUT ya está registrado");
            }
            user.setRut(dto.getRut());
        }

        if (dto.getTelefono() != null) {
            user.setTelefono(dto.getTelefono());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            PasswordPolicy.validate(dto.getPassword());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }

    // ── ROLES ─────────────────────────────────────────────────────────────────────

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void createRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new BusinessException("Debes ingresar el nombre del rol");
        }
        String formatted = roleName.toUpperCase();
        if (!formatted.startsWith("ROLE_")) {
            formatted = "ROLE_" + formatted;
        }
        if (roleRepository.findByNombre(formatted).isPresent()) {
            throw new BusinessException("El rol '" + formatted + "' ya existe");
        }
        roleRepository.save(new Role(null, formatted));
    }

    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Role role = roleRepository.findByNombre(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void revokeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Role userRole = roleRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol base no encontrado"));
        user.setRole(userRole);
        userRepository.save(user);
    }

    // ── MÓDULOS ──────────────────────────────────────────────────────────────────

    public List<Modulo> getAllModulos() {
        return moduloRepository.findAll();
    }

    public List<Acceso> getAllAccesos() {
        return accesoRepository.findAll();
    }

    // ── PERMISOS (ROL ↔ MÓDULO) ──────────────────────────────────────────────────

    @Transactional
    public void grantPermissionToRole(Long idRol, Long idModulo, Long idAcceso) {
        Role role = roleRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
        Modulo modulo = moduloRepository.findById(idModulo)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado"));
        Acceso acceso = accesoRepository.findById(idAcceso)
                .orElseThrow(() -> new ResourceNotFoundException("Acceso no encontrado"));

        RoleModulo.RoleModuloId id = buildId(idRol, idModulo, idAcceso);

        if (!roleModuloRepository.existsById(id)) {
            RoleModulo rm = new RoleModulo();
            rm.setId(id);
            rm.setRol(role);
            rm.setModulo(modulo);
            rm.setAcceso(acceso);
            roleModuloRepository.save(rm);
        }
    }

    @Transactional
    public void revokePermissionFromRole(Long idRol, Long idModulo, Long idAcceso) {
        RoleModulo.RoleModuloId id = buildId(idRol, idModulo, idAcceso);
        if (roleModuloRepository.existsById(id)) {
            roleModuloRepository.deleteById(id);
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────────

    private Role resolveRole(UserRegisterDTO dto, String defaultRole) {
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            String roleName = dto.getRoles().iterator().next();
            return roleRepository.findByNombre(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        }
        return roleRepository.findByNombre(defaultRole)
                .orElseThrow(() -> new ResourceNotFoundException("Rol base no encontrado"));
    }

    private RoleModulo.RoleModuloId buildId(Long idRol, Long idModulo, Long idAcceso) {
        RoleModulo.RoleModuloId id = new RoleModulo.RoleModuloId();
        id.setIdRol(idRol);
        id.setIdModulo(idModulo);
        id.setIdAcceso(idAcceso);
        return id;
    }
}
