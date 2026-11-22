package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.Cliente;
import com.raizesDoNordeste.raizesDoNordeste.services.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public record CadastroClienteRequest(
            String nome,
            String email,
            String senha
    ) {
    }

// REGRA DE NEGÓCIO - Permite cadastrar clientes com dados válidos.
    @PostMapping
    public ResponseEntity<Cliente> cadastrarCliente(
            @RequestBody CadastroClienteRequest dados) {

        Cliente cliente = clienteService.cadastrarCliente(
                dados.nome(),
                dados.email(),
                dados.senha()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

// REGRA DE NEGÓCIO - Registra a decisão do cliente sobre a participação na fidelização.
    @PatchMapping("/{id}/consentimento")
    public ResponseEntity<Cliente> atualizarConsentimento(
            @PathVariable Long id,
            @RequestParam Boolean consentimento) {

        Cliente cliente = clienteService.atualizarConsentimentoFidelidade(
                id,
                consentimento
        );

        return ResponseEntity.ok(cliente);
    }
}