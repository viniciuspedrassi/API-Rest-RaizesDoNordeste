package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.Usuario;
import com.raizesDoNordeste.raizesDoNordeste.services.TokenService;
import com.raizesDoNordeste.raizesDoNordeste.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    public AuthController(UsuarioService usuarioService,
                          TokenService tokenService) {

        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

// REGRA DE NEGÓCIO - Somente credenciais válidas permitem gerar um JWT.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest dados) {

        Usuario usuario = usuarioService.autenticarUsuario(
                dados.email(),
                dados.senha()
        );

        String token = tokenService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    public record LoginRequest(String email, String senha) {
    }

    public record LoginResponse(String token) {
    }
}