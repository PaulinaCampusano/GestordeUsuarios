package com.gestion.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    @NotBlank(message = "Debes ingresar tu contraseña actual")
    private String currentPassword;

    @NotBlank(message = "Debes ingresar la nueva contraseña")
    private String newPassword;
}
