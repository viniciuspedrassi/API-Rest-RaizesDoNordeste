package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.PerfilUsuario;
import com.raizesDoNordeste.raizesDoNordeste.entities.Usuario;
import com.raizesDoNordeste.raizesDoNordeste.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

// REGRA DE NEGÓCIO - Somente administradores autorizados podem cadastrar funcionários.
    @PostMapping
    public ResponseEntity<Usuario> cadastrarUsuario(
            @RequestBody CadastroUsuarioRequest dados) {

        Usuario usuario = usuarioService.cadastrarUsuario(
                dados.nome(),
                dados.email(),
                dados.senha(),
                dados.perfil(),
                dados.unidadeId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    public record CadastroUsuarioRequest(
            String nome,
            String email,
            String senha,
            PerfilUsuario perfil,
            Long unidadeId) {
    }
}