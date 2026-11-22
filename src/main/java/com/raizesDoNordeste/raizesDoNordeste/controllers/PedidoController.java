package com.raizesDoNordeste.raizesDoNordeste.controllers;

import com.raizesDoNordeste.raizesDoNordeste.entities.CanalPedido;
import com.raizesDoNordeste.raizesDoNordeste.entities.Pedido;
import com.raizesDoNordeste.raizesDoNordeste.entities.StatusPedido;
import com.raizesDoNordeste.raizesDoNordeste.services.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // REGRA DE NEGÓCIO - Registra pedidos com vários produtos,
    // mantendo o status inicial aguardando pagamento.
    @PostMapping
    public ResponseEntity<Pedido> registrarPedido(
            @RequestParam Long unidadeId,
            @RequestParam CanalPedido canalPedido,
            @RequestParam List<Long> produtoIds,
            @RequestParam List<Integer> quantidades,
            @RequestParam(required = false) Long clienteId) {

        Pedido pedido = pedidoService.registrarPedido(
                unidadeId,
                canalPedido,
                produtoIds,
                quantidades,
                clienteId
        );
        return ResponseEntity.ok(pedido);
    }
// REGRA DE NEGÓCIO - Consulta pedidos filtrados pelo canal de origem.
    @GetMapping
    public ResponseEntity<List<Pedido>> consultarPorCanal(
            @RequestParam CanalPedido canalPedido) {

        List<Pedido> pedidos = pedidoService.consultarPorCanal(canalPedido);

        return ResponseEntity.ok(pedidos);
    }

// REGRA DE NEGÓCIO - Permite atualizar o status operacional de um pedido.
    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido novoStatus) {

        Pedido pedido = pedidoService.atualizarStatus(id, novoStatus);

        return ResponseEntity.ok(pedido);
    }
}