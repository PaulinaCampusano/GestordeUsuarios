package com.gestion.usuario.exception;

public class RutAlreadyExistsException extends RuntimeException {
    public RutAlreadyExistsException(String message) {
        super(message);
    }
}
