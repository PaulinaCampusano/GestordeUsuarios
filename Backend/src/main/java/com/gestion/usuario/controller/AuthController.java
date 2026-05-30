package com.gestion.usuario.controller;

import com.gestion.usuario.dto.*;
import com.gestion.usuario.entity.User;
import com.gestion.usuario.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro, login y recuperación de credenciales. No requieren token JWT.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar nuevo usuario",
               description = "Crea una nueva cuenta. El usuario queda inactivo hasta que un administrador lo active.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación"),
            @ApiResponse(responseCode = "409", description = "El correo o RUT ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @Parameter(description = "Datos del nuevo usuario") @Valid @RequestBody UserRegisterDTO registerDTO) {
        authService.registerUser(registerDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario registrado exitosamente. Espera la activación de tu cuenta.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Iniciar sesión",
               description = "Autentica con correo y contraseña. Retorna un Token JWT con los permisos del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Cuenta pendiente de activación")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Parameter(description = "Credenciales de acceso") @Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Cerrar sesión",
               description = "Limpia el contexto de seguridad. El cliente debe eliminar el token JWT del lado frontend.")
    @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Sesión cerrada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Solicitar recuperación de contraseña",
               description = "Genera un token de un solo uso (TTL 15 min). Siempre retorna 200 para evitar enumeración de usuarios.")
    @ApiResponse(responseCode = "200", description = "Solicitud procesada")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> requestReset(@Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            authService.requestPasswordReset(requestDTO.getEmail());
        } catch (RuntimeException ignored) {
            // Silenciado por seguridad — no revelar si el correo existe
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el correo existe, se ha enviado un enlace de recuperación.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Establecer nueva contraseña",
               description = "Valida el token de recuperación y establece la nueva contraseña. El token queda destruido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o contraseña no cumple los requisitos")
    })
    @PostMapping("/new-password")
    public ResponseEntity<Map<String, String>> saveNewPassword(@Valid @RequestBody NewPasswordDTO requestDTO) {
        authService.resetPassword(requestDTO.getToken(), requestDTO.getNewPassword());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña actualizada exitosamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(Principal principal) {
        User user = authService.getMe(principal.getName());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("nombreCompleto", user.getNombreCompleto());
        response.put("email", user.getEmail());
        response.put("rut", user.getRut());
        response.put("telefono", user.getTelefono());
        response.put("rol", user.getRole() != null ? user.getRole().getNombre() : "SIN_ROL");
        response.put("activo", user.getIsActive());
        response.put("fechaCreacion", user.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar perfil del usuario autenticado")
    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> updateMe(
            Principal principal,
            @Valid @RequestBody AdminUpdateDTO dto) {
        authService.updateMe(principal.getName(), dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Perfil actualizado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cambiar contraseña del usuario autenticado",
               description = "Valida la contraseña actual, aplica política de complejidad y actualiza.")
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changeMyPassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordDTO dto) {
        authService.changeMyPassword(principal.getName(), dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña actualizada correctamente");
        return ResponseEntity.ok(response);
    }
}
