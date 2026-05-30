package com.gestion.usuario.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateDTO {

    @Email(message = "Formato de correo inválido")
    private String email;

    private String nombreCompleto;

    private String rut;

    private String telefono;

    private String password;

    /**
     * Returns true if all updatable fields are null or blank.
     */
    public boolean isEmpty() {
        return isBlankOrNull(email)
                && isBlankOrNull(nombreCompleto)
                && isBlankOrNull(rut)
                && isBlankOrNull(telefono)
                && isBlankOrNull(password);
    }

    private boolean isBlankOrNull(String value) {
        return value == null || value.isBlank();
    }
}
