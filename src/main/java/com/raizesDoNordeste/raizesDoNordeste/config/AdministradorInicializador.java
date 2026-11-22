package com.raizesDoNordeste.raizesDoNordeste.config;

import com.raizesDoNordeste.raizesDoNordeste.entities.PerfilUsuario;
import com.raizesDoNordeste.raizesDoNordeste.repositories.UsuarioRepository;
import com.raizesDoNordeste.raizesDoNordeste.services.UsuarioService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("bootstrap-admin")
public class AdministradorInicializador implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public AdministradorInicializador(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {

// REGRA DE NEGÓCIO - Permite criar somente o primeiro administrador.
        if (usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)) {
            return;
        }

        String email = System.getenv("ADMIN_EMAIL");
        String senha = System.getenv("ADMIN_PASSWORD");

        // VALIDAÇÃO - Exige credenciais configuradas no ambiente.
        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {

            // TRATAMENTO - Impede a criação com credenciais ausentes.
            throw new IllegalStateException(
                    "Configure ADMIN_EMAIL e ADMIN_PASSWORD."
            );
        }

        usuarioService.cadastrarUsuario(
                "Administrador Inicial",
                email,
                senha,
                PerfilUsuario.ADMINISTRADOR,
                null
        );
    }
}