package com.gestion.usuario.repository;

import com.gestion.usuario.entity.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccesoRepository extends JpaRepository<Acceso, Long> {
}
