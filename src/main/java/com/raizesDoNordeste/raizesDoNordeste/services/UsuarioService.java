package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.repositories.UsuarioRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.UnidadeRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.raizesDoNordeste.raizesDoNordeste.entities.PerfilUsuario;
import com.raizesDoNordeste.raizesDoNordeste.entities.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.raizesDoNordeste.raizesDoNordeste.entities.Unidade;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final BCryptPasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UnidadeRepository unidadeRepository) {

        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.encoder = new BCryptPasswordEncoder();
    }

    // REGRA DE NEGÓCIO - Todo funcionário deve possuir dados válidos e utilizar um e-mail ainda não cadastrado.
    public Usuario cadastrarUsuario(
            String nome,
            String email,
            String senha,
            PerfilUsuario perfil,
            Long unidadeId) {

        // VALIDAÇÃO - Verifica se o nome foi informado.
        if (nome == null || nome.isBlank()) {

            // TRATAMENTO - Rejeita cadastro sem nome.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome deve ser informado."
            );
        }

        // VALIDAÇÃO - Verifica se o e-mail foi informado.
        if (email == null || email.isBlank()) {

            // TRATAMENTO - Rejeita cadastro sem e-mail.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail deve ser informado."
            );
        }

        // VALIDAÇÃO - Verifica se a senha foi informada.
        if (senha == null || senha.isBlank()) {

            // TRATAMENTO - Rejeita cadastro sem senha.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve ser informada."
            );
        }

        // VALIDAÇÃO - Verifica se o perfil foi informado.
        if (perfil == null) {

            // TRATAMENTO - Rejeita cadastro sem perfil.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O perfil deve ser informado."
            );
        }

        // VALIDAÇÃO - Verifica se o e-mail já está cadastrado.
        if (usuarioRepository.existsByEmail(email)) {

            // TRATAMENTO - Impede cadastro com e-mail duplicado.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado."
            );
        }

        // VALIDAÇÃO - Verifica se o e-mail possui um formato básico válido.
        if (!email.contains("@") || !email.contains(".")) {
            // TRATAMENTO - Rejeita e-mail inválido.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail informado é inválido."
            );
        }

        // VALIDAÇÃO - Exige uma senha com pelo menos 8 caracteres.
        if (senha.length() < 8) {
            // TRATAMENTO - Rejeita senhas muito curtas.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve possuir pelo menos 8 caracteres."
            );
        }

        Unidade unidade = null;

// REGRA DE NEGÓCIO - Funcionários devem pertencer a uma unidade, exceto o ADMINISTRADOR.
        if (perfil != PerfilUsuario.ADMINISTRADOR && unidadeId == null) {
            // TRATAMENTO - Impede funcionários sem unidade.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A unidade deve ser informada."
            );
        }

        // VALIDAÇÃO - Verifica se a unidade informada existe.
        if (unidadeId != null) {

            unidade = unidadeRepository.findById(unidadeId).orElse(null);

            if (unidade == null) {
                // TRATAMENTO - Rejeita uma unidade inexistente.
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unidade não encontrada."
                );
            }
        }

// REGRA DE NEGÓCIO - A senha deve ser armazenada como hash BCrypt.
        String senhaHash = encoder.encode(senha);

        Usuario usuario = new Usuario(
                nome,
                email,
                senhaHash,
                perfil,
                unidade
        );

        return usuarioRepository.save(usuario);
    }

    public boolean verificarCredenciais(String email, String senha) {

        // VALIDAÇÃO - Verifica se as credenciais foram informadas.
        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {
            return false;
        }

        // VALIDAÇÃO - Verifica se o funcionário existe.
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            return false;
        }

// REGRA DE NEGÓCIO - Confere a senha sem descriptografar o hash.
        return encoder.matches(senha, usuario.getSenhaHash());
    }

    public Usuario autenticarUsuario(String email, String senha) {

        // VALIDAÇÃO - Verifica se as credenciais são válidas.
        if (!verificarCredenciais(email, senha)) {

            // TRATAMENTO - Rejeita credenciais inválidas.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos."
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        // VALIDAÇÃO - Verifica se o funcionário foi encontrado.
        if (usuario == null) {

            // TRATAMENTO - Impede o login de um funcionário inexistente.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos."
            );
        }

        return usuario;
    }

}