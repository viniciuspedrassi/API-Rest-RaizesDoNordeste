package com.raizesDoNordeste.raizesDoNordeste.repositories;

import com.raizesDoNordeste.raizesDoNordeste.entities.PerfilUsuario;
import com.raizesDoNordeste.raizesDoNordeste.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPerfil(PerfilUsuario perfil);
}