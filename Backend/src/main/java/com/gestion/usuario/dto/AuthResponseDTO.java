package com.gestion.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String type;
    private String email;
    private String nombreCompleto;
    private String rol;
    private List<ModulePermissionDTO> permisos;
}
