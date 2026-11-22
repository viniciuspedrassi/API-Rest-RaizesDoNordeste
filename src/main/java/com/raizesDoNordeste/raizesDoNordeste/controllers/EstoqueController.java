package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.Estoque;
import com.raizesDoNordeste.raizesDoNordeste.services.EstoqueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping ("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // REGRA DE NEGÓCIO - Registra a entrada de produtos no estoque da unidade e mantém o histórico da movimentação.
    @PostMapping("/entradas")
    public ResponseEntity<Estoque> registrarEntradaEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long unidadeId,
            @RequestParam Integer quantidade) {

        Estoque estoque = estoqueService.registrarEntradaEstoque(
                produtoId, unidadeId, quantidade
        );

        return ResponseEntity.ok(estoque);
    }

// REGRA DE NEGÓCIO - Registra a saída de produtos no estoque da unidade e mantém o histórico da movimentação.
    @PostMapping("/saidas")
    public ResponseEntity<Estoque> registrarSaidaEstoque(
            @RequestParam Long produtoId,
            @RequestParam Long unidadeId,
            @RequestParam Integer quantidade) {

        Estoque estoque = estoqueService.registrarSaidaEstoque(
                produtoId, unidadeId, quantidade
        );

        return ResponseEntity.ok(estoque);
    }

// REGRA DE NEGÓCIO - Permite a consulta do saldo de um produto em uma unidade, sem alterar o estoque.
    @GetMapping
    public ResponseEntity<Estoque> buscarPorProdutoEUnidade(
            @RequestParam Long produtoId,
            @RequestParam Long unidadeId) {

        Optional<Estoque> estoqueEncontrado =
                estoqueService.buscarPorProdutoEUnidade(produtoId, unidadeId);

        // VALIDAÇÃO - Verifica se existe estoque do produto na unidade.
        if (estoqueEncontrado.isEmpty()) {

            // TRATAMENTO - Retorna HTTP 404 quando o estoque não existe.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Estoque não encontrado para este produto e unidade."
            );
        }

        return ResponseEntity.ok(estoqueEncontrado.get());
    }



}
