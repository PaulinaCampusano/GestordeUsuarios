package com.gestion.usuario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de correos electrónicos.
 * Por defecto simula el envío imprimiendo en consola.
 * Para integrar con un proveedor SMTP real, reemplaza el método
 * sendPasswordResetEmail con JavaMailSender o similar.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendPasswordResetEmail(String email, String token) {
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        log.info("========================================");
        log.info("📧 [SIMULACIÓN DE CORREO]");
        log.info("Para: {}", email);
        log.info("Asunto: Recuperación de contraseña");
        log.info("Enlace de recuperación (válido 15 min):");
        log.info("{}", resetLink);
        log.info("========================================");
    }
}
