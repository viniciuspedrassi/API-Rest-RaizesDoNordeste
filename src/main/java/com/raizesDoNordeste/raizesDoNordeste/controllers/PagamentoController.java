package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.Pedido;
import com.raizesDoNordeste.raizesDoNordeste.services.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

// REGRA DE NEGÓCIO - Processa o pagamento simulado e atualiza o pedido conforme o resultado.
    @PostMapping("/simular")
    public ResponseEntity<Pedido> simularPagamento(
            @RequestParam Long pedidoId,
            @RequestParam Boolean aprovado,
            @RequestParam(required = false, defaultValue = "0") Integer pontosResgatar) {

        Pedido pedido = pagamentoService.simularPagamento(
                pedidoId,
                aprovado,
                pontosResgatar
        );

        return ResponseEntity.ok(pedido);
    }
}