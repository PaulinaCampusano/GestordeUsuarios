package com.gestion.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ModulePermissionDTO {
    private String modulo;
    private String rutaFrontend;
    private List<String> acciones;
}
