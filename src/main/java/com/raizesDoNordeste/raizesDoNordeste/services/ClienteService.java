package com.raizesDoNordeste.raizesDoNordeste.services;


import com.raizesDoNordeste.raizesDoNordeste.entities.Cliente;
import com.raizesDoNordeste.raizesDoNordeste.repositories.ClienteRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder encoder;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
        this.encoder = new BCryptPasswordEncoder();
    }

// REGRA DE NEGÓCIO - Cadastra clientes com dados válidos e senha protegida por BCrypt.
    public Cliente cadastrarCliente(String nome, String email, String senha) {

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

        // VALIDAÇÃO - Verifica se o e-mail possui um formato básico válido.
        if (!email.contains("@") || !email.contains(".")) {

            // TRATAMENTO - Rejeita e-mail inválido.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail informado é inválido."
            );
        }

        // VALIDAÇÃO - Verifica se o e-mail já está cadastrado.
        if (clienteRepository.existsByEmail(email)) {

            // TRATAMENTO - Impede cadastro com e-mail duplicado.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado."
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

// REGRA DE NEGÓCIO - Armazena a senha como hash BCrypt.
        String senhaHash = encoder.encode(senha);

        Cliente cliente = new Cliente(nome, email, senhaHash);

        // REGRA DE NEGÓCIO - Todo cliente inicia sem pontos e sem adesão à fidelização.
        cliente.setPontosFidelidade(0);
        cliente.setConsentimentoFidelidade(false);
        cliente.setDataConsentimentoFidelidade(null);

        return clienteRepository.save(cliente);
    }

// REGRA DE NEGÓCIO - Registra a autorização ou retirada do consentimento para fidelização.
    public Cliente atualizarConsentimentoFidelidade(Long clienteId, Boolean consentimento) {

        // VALIDAÇÃO - Verifica se o ID do cliente foi informado.
        if (clienteId == null) {

            // TRATAMENTO - Rejeita operações sem identificador do cliente.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O ID do cliente deve ser informado."
            );
        }

        // VALIDAÇÃO - Verifica se a decisão sobre o consentimento foi informada.
        if (consentimento == null) {

            // TRATAMENTO - Rejeita operações sem consentimento definido.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O consentimento deve ser informado."
            );
        }

        Optional<Cliente> clienteEncontrado = clienteRepository.findById(clienteId);

        // VALIDAÇÃO - Verifica se o cliente existe.
        if (clienteEncontrado.isEmpty()) {

            // TRATAMENTO - Retorna HTTP 404 para cliente inexistente.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente não encontrado."
            );
        }

        Cliente cliente = clienteEncontrado.get();

// REGRA DE NEGÓCIO - Registra a adesão e a data em que ela ocorreu.
        if (consentimento) {

            boolean jaConsentiu =
                    Boolean.TRUE.equals(cliente.getConsentimentoFidelidade());

            cliente.setConsentimentoFidelidade(true);

            if (!jaConsentiu
                    || cliente.getDataConsentimentoFidelidade() == null) {

                cliente.setDataConsentimentoFidelidade(LocalDateTime.now());
            }

        } else {

// REGRA DE NEGÓCIO - Permite retirar a autorização sem apagar os pontos existentes.
            cliente.setConsentimentoFidelidade(false);
            cliente.setDataConsentimentoFidelidade(null);
        }

        return clienteRepository.save(cliente);
    }

}