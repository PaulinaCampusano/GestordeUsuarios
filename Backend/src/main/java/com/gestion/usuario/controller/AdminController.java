package com.gestion.usuario.controller;

import com.gestion.usuario.dto.AdminUpdateDTO;
import com.gestion.usuario.dto.PermissionRequestDTO;
import com.gestion.usuario.dto.UserRegisterDTO;
import com.gestion.usuario.entity.Acceso;
import com.gestion.usuario.entity.Modulo;
import com.gestion.usuario.entity.Role;
import com.gestion.usuario.entity.User;
import com.gestion.usuario.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración (IAM)",
     description = "Endpoints protegidos para gestión de usuarios, roles y permisos. Requiere ROLE_ADMIN.")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ── USUARIOS ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Mapa id→nombre de usuarios activos",
               description = "Accesible también a ROLE_SUPERVISOR para resolver IDs en vistas de supervisión.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @GetMapping("/users/nombres")
    public ResponseEntity<List<Map<String, Object>>> getUserNombres() {
        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .map(u -> Map.<String, Object>of("id", u.getId(), "nombre", u.getNombreCompleto()))
                        .toList());
    }

    @Operation(summary = "Crear usuario (Aprovisionamiento directo)")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @Parameter(description = "Datos del nuevo usuario") @Valid @RequestBody UserRegisterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.adminCreateUser(dto));
    }

    @Operation(summary = "Editar datos de un usuario",
               description = "Todos los campos son opcionales — debe enviarse al menos uno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Body vacío o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Correo o RUT ya en uso")
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateDTO dto) {
        userService.updateUser(id, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario actualizado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activar / Desactivar usuario (Soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        userService.toggleUserStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Estado del usuario actualizado");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Asignar rol a usuario")
    @PostMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<Map<String, String>> assignRole(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @Parameter(description = "Nombre del rol (ej. ROLE_SUPERVISOR)") @PathVariable String roleName) {
        userService.assignRoleToUser(userId, roleName);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol asignado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revocar rol a usuario (degrada a ROLE_USER)")
    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<Map<String, String>> revokeRole(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @Parameter(description = "Rol a revocar") @PathVariable String roleName) {
        userService.revokeRoleFromUser(userId, roleName);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol " + roleName + " revocado al usuario " + userId);
        return ResponseEntity.ok(response);
    }

    // ── ROLES ─────────────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los roles")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }

    @Operation(summary = "Crear nuevo rol")
    @PostMapping("/roles")
    public ResponseEntity<Map<String, String>> createRole(
            @Parameter(description = "Nombre del rol (se agrega ROLE_ automáticamente si falta)")
            @RequestParam String roleName) {
        userService.createRole(roleName);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol creado exitosamente");
        return ResponseEntity.ok(response);
    }

    // ── MÓDULOS Y ACCESOS ─────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los módulos")
    @GetMapping("/modules")
    public ResponseEntity<List<Modulo>> getAllModulos() {
        return ResponseEntity.ok(userService.getAllModulos());
    }

    @Operation(summary = "Listar todos los tipos de acceso")
    @GetMapping("/access-types")
    public ResponseEntity<List<Acceso>> getAllAccesos() {
        return ResponseEntity.ok(userService.getAllAccesos());
    }

    // ── MATRIZ DE PERMISOS ────────────────────────────────────────────────────────

    @Operation(summary = "Otorgar permiso a un rol")
    @PostMapping("/roles/{idRol}/permissions")
    public ResponseEntity<Map<String, String>> grantPermission(
            @Parameter(description = "ID del rol") @PathVariable Long idRol,
            @RequestBody PermissionRequestDTO dto) {
        userService.grantPermissionToRole(idRol, dto.getIdModulo(), dto.getIdAcceso());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso otorgado exitosamente al rol");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revocar permiso de un rol")
    @DeleteMapping("/roles/{idRol}/permissions")
    public ResponseEntity<Map<String, String>> revokePermission(
            @Parameter(description = "ID del rol") @PathVariable Long idRol,
            @RequestBody PermissionRequestDTO dto) {
        userService.revokePermissionFromRole(idRol, dto.getIdModulo(), dto.getIdAcceso());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso revocado exitosamente del rol");
        return ResponseEntity.ok(response);
    }
}
