package com.gestion.usuario.config;

import com.gestion.usuario.entity.*;
import com.gestion.usuario.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModuloRepository moduloRepository;
    private final AccesoRepository accesoRepository;
    private final RoleModuloRepository roleModuloRepository;

    public DataSeeder(UserRepository userRepository,
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

    @Override
    @Transactional
    public void run(String... args) {
        // ── Roles ───────────────────────────────────────────────────────────────
        Role adminRole    = findOrCreateRole("ROLE_ADMIN",   "Administrador del sistema");
        Role userRole     = findOrCreateRole("ROLE_USER",    "Usuario estándar");
        Role supervisorRole = findOrCreateRole("ROLE_SUPERVISOR", "Supervisor de acceso de solo lectura");

        // ── Módulos ─────────────────────────────────────────────────────────────
        Modulo moduloUsuarios = findOrCreateModulo("Gestión de Usuarios",
                "Módulo principal de administración de usuarios", "/usuarios");
        Modulo moduloRoles = findOrCreateModulo("Gestión de Roles",
                "Módulo para administrar roles y permisos", "/roles");

        // ── Tipos de acceso ──────────────────────────────────────────────────────
        Acceso crear      = findOrCreateAcceso("CREAR",     "Permite crear registros");
        Acceso leer       = findOrCreateAcceso("LEER",      "Permite visualizar información");
        Acceso actualizar = findOrCreateAcceso("ACTUALIZAR","Permite modificar registros");
        Acceso eliminar   = findOrCreateAcceso("ELIMINAR",  "Permite eliminar o desactivar registros");

        // ── Permisos por rol ─────────────────────────────────────────────────────
        // Admin: acceso total a ambos módulos
        asignarPermiso(adminRole, moduloUsuarios, crear);
        asignarPermiso(adminRole, moduloUsuarios, leer);
        asignarPermiso(adminRole, moduloUsuarios, actualizar);
        asignarPermiso(adminRole, moduloUsuarios, eliminar);
        asignarPermiso(adminRole, moduloRoles, crear);
        asignarPermiso(adminRole, moduloRoles, leer);
        asignarPermiso(adminRole, moduloRoles, actualizar);
        asignarPermiso(adminRole, moduloRoles, eliminar);

        // Supervisor: solo lectura
        asignarPermiso(supervisorRole, moduloUsuarios, leer);
        asignarPermiso(supervisorRole, moduloRoles, leer);

        // User: solo puede ver su propio perfil (no tiene permisos de módulo)

        // ── Usuarios de prueba ───────────────────────────────────────────────────
        crearUsuarioSiNoExiste("admin@gestion.com",   "Administrador Maestro", "AdminPass123!", adminRole);
        crearUsuarioSiNoExiste("user@gestion.com",    "Usuario Estándar",      "UserPass123!",  userRole);
        crearUsuarioSiNoExiste("supervisor@gestion.com", "Supervisor del Sistema", "Supervisor123!", supervisorRole);

        System.out.println("✅ SEMILLA: Datos iniciales cargados correctamente.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Role findOrCreateRole(String nombre, String descripcion) {
        return roleRepository.findByNombre(nombre).orElseGet(() -> {
            Role r = new Role();
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            return roleRepository.save(r);
        });
    }

    private Modulo findOrCreateModulo(String nombre, String descripcion, String ruta) {
        return moduloRepository.findAll().stream()
                .filter(m -> m.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Modulo m = new Modulo();
                    m.setNombre(nombre);
                    m.setDescripcion(descripcion);
                    m.setRutaFrontend(ruta);
                    return moduloRepository.save(m);
                });
    }

    private Acceso findOrCreateAcceso(String nombre, String descripcion) {
        return accesoRepository.findAll().stream()
                .filter(a -> a.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Acceso a = new Acceso();
                    a.setNombre(nombre);
                    a.setDescripcion(descripcion);
                    return accesoRepository.save(a);
                });
    }

    private void asignarPermiso(Role rol, Modulo modulo, Acceso acceso) {
        RoleModulo.RoleModuloId id = new RoleModulo.RoleModuloId();
        id.setIdRol(rol.getId());
        id.setIdModulo(modulo.getId());
        id.setIdAcceso(acceso.getId());

        if (!roleModuloRepository.existsById(id)) {
            RoleModulo rm = new RoleModulo();
            rm.setId(id);
            rm.setRol(rol);
            rm.setModulo(modulo);
            rm.setAcceso(acceso);
            roleModuloRepository.save(rm);
        }
    }

    private void crearUsuarioSiNoExiste(String email, String nombre, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setNombreCompleto(nombre);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setIsActive(true);
            userRepository.save(user);
        }
    }
}
