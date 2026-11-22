package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.entities.*;
import com.raizesDoNordeste.raizesDoNordeste.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UnidadeRepository unidadeRepository;
    private final EstoqueRepository estoqueRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ItemPedidoRepository itemPedidoRepository,
                         ProdutoRepository produtoRepository,
                         UnidadeRepository unidadeRepository,
                         EstoqueRepository estoqueRepository, ClienteRepository clienteRepository) {

        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.unidadeRepository = unidadeRepository;
        this.estoqueRepository = estoqueRepository;
        this.clienteRepository = clienteRepository;
    }
    // REGRA DE NEGÓCIO - Garante que o pedido e seus itens sejam salvos na mesma transação.
// REGRA DE NEGÓCIO - Todo pedido deve ter um produto, com uma quantidade correspondente para cada item.
    @Transactional
    public Pedido registrarPedido(
            Long unidadeId,
            CanalPedido canalPedido,
            List<Long> produtoIds,
            List<Integer> quantidades,
            Long clienteId) {

        // VALIDAÇÃO - Verifica se o pedido possui produtos.
        if (produtoIds == null || produtoIds.isEmpty()) {

            // TRATAMENTO - Rejeita pedidos sem produtos.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O pedido deve conter pelo menos um produto."
            );
        }

        // VALIDAÇÃO - Verifica se cada produto possui uma quantidade correspondente.
        if (quantidades == null || produtoIds.size() != quantidades.size()) {

            // TRATAMENTO - Rejeita lista de produtos que não correspondam as quantidades.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A lista de produtos deve corresponder à lista de quantidades."
            );
        }
// REGRA DE NEGÓCIO - Todo pedido deve estar vinculado a uma unidade existente.

        // VALIDAÇÃO - Verifica se o ID da unidade foi informado.
        if (unidadeId == null) {

            // TRATAMENTO - Rejeita pedidos sem unidade com HTTP 400.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A unidade deve ser informada."
            );
        }

        Optional<Unidade> unidadeEncontrada = unidadeRepository.findById(unidadeId);

        // VALIDAÇÃO - Verifica se a unidade existe.
        if (unidadeEncontrada.isEmpty()) {

            // TRATAMENTO - Avisa que a unidade não foi encontrada.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unidade não encontrada."
            );
        }

        // VALIDAÇÃO - Verifica se o canal do pedido foi informado.
        if (canalPedido == null) {

            // TRATAMENTO - Rejeita pedidos sem canal informado.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O canal do pedido deve ser informado."
            );
        }

// REGRA DE NEGÓCIO - Agrupa as quantidades de produtos repetidos no pedido.
        Map<Long, Integer> quantidadesPorProduto = new HashMap<>();

// REGRA DE NEGÓCIO - Todos os itens do pedido devem possuir quantidades positivas.
        for (int i = 0; i < produtoIds.size(); i++) {

            Integer quantidade = quantidades.get(i);

            // VALIDAÇÃO - Verifica se a quantidade do item é maior que zero.
            if (quantidade == null || quantidade <= 0) {

                // TRATAMENTO - Rejeita itens com quantidades inválidas.
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A quantidade de cada produto deve ser maior que zero."
                );
            }
            Long produtoId = produtoIds.get(i);

            // VALIDAÇÃO - Verifica se o ID do produto foi informado.
            if (produtoId == null) {

                // TRATAMENTO - Rejeita produtos sem identificador.
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O ID do produto deve ser informado."
                );
            }
            Optional<Produto> produtoEncontrado = produtoRepository.findById(produtoId);

            // VALIDAÇÃO - Verifica se o produto existe no banco de dados.
            if (produtoEncontrado.isEmpty()) {

                // TRATAMENTO - Retorna HTTP 404 quando o produto não for encontrado.
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                );
            }

            Optional<Estoque> estoqueEncontrado =
                    estoqueRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId);

            // VALIDAÇÃO - Verifica se existe estoque do produto na unidade.
            if (estoqueEncontrado.isEmpty()) {

                // TRATAMENTO - Impede pedidos de produtos sem estoque cadastrado.
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Estoque não encontrado para este produto na unidade."
                );
            }

// REGRA DE NEGÓCIO - Soma as quantidades solicitadas para o mesmo produto.
            int total = quantidadesPorProduto.getOrDefault(produtoId, 0);

            quantidadesPorProduto.put(produtoId, total + quantidade);
        }

// REGRA DE NEGÓCIO - Impede pedidos com quantidades superiores ao estoque disponível.
        for (Map.Entry<Long, Integer> item : quantidadesPorProduto.entrySet()) {

            Long produtoId = item.getKey();
            Integer quantidadeTotal = item.getValue();

            Optional<Estoque> estoqueEncontrado =
                    estoqueRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId);

            Estoque estoque = estoqueEncontrado.get();

            // VALIDAÇÃO - Verifica se o total solicitado ultrapassa o saldo disponível.
            if (quantidadeTotal > estoque.getQuantidade()) {

                // TRATAMENTO - Rejeita o pedido quando o estoque é insuficiente.
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Estoque insuficiente para o produto de ID " + produtoId + "."
                );
            }
        }

        Cliente cliente = null;

// REGRA DE NEGÓCIO - Permite vincular um cliente cadastrado ao pedido.
        if (clienteId != null) {

            Optional<Cliente> clienteEncontrado =
                    clienteRepository.findById(clienteId);

            // VALIDAÇÃO - Verifica se o cliente informado existe.
            if (clienteEncontrado.isEmpty()) {

                // TRATAMENTO - Retorna HTTP 404 para cliente inexistente.
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado."
                );
            }

            cliente = clienteEncontrado.get();
        }
// REGRA DE NEGÓCIO - Todo pedido inicia aguardando pagamento, com data e hora registradas pelo sistema.
        Pedido pedido = new Pedido(
                LocalDateTime.now(),
                StatusPedido.AGUARDANDO_PAGAMENTO,
                canalPedido,
                cliente,
                unidadeEncontrada.get(),
                null
        );
        pedido = pedidoRepository.save(pedido);

// REGRA DE NEGÓCIO - Registra os itens do pedido com os preços praticados no momento da compra.
        for (int i = 0; i < produtoIds.size(); i++) {

            Long produtoId = produtoIds.get(i);
            Integer quantidade = quantidades.get(i);

            Produto produto = produtoRepository.findById(produtoId).get();

            ItemPedido itemPedido = new ItemPedido(
                    pedido,
                    produto,
                    quantidade,
                    produto.getPreco()
            );

            itemPedidoRepository.save(itemPedido);
        }

        return pedido;
    }
// REGRA DE NEGÓCIO - Permite consultar pedidos pelo canal de origem.
    public List<Pedido> consultarPorCanal(CanalPedido canalPedido) {

        // VALIDAÇÃO - Verifica se o canal foi informado.
        if (canalPedido == null) {

            // TRATAMENTO - Rejeita consultas sem canal.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O canal do pedido deve ser informado."
            );
        }

        return pedidoRepository.findByCanalPedido(canalPedido);
    }

// REGRA DE NEGÓCIO - Permite avançar o pedido somente pelas etapas operacionais autorizadas.
    @Transactional
    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {

        // VALIDAÇÃO - Verifica se o ID do pedido foi informado.
        if (pedidoId == null) {

            // TRATAMENTO - Rejeita pedidos sem identificador.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O ID do pedido deve ser informado."
            );
        }

        // VALIDAÇÃO - Verifica se o novo status foi informado.
        if (novoStatus == null) {

            // TRATAMENTO - Rejeita alterações sem status.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O novo status deve ser informado."
            );
        }

        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(pedidoId);

        // VALIDAÇÃO - Verifica se o pedido existe.
        if (pedidoEncontrado.isEmpty()) {

            // TRATAMENTO - Retorna HTTP 404 para pedido inexistente.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pedido não encontrado."
            );
        }

        Pedido pedido = pedidoEncontrado.get();

        boolean transicaoValida = false;

// REGRA DE NEGÓCIO - Pedido recebido pode iniciar a preparação.
        if (pedido.getStatus() == StatusPedido.RECEBIDO
                && novoStatus == StatusPedido.EM_PREPARACAO) {

            transicaoValida = true;

// REGRA DE NEGÓCIO - Pedido em preparação pode ficar pronto.
        } else if (pedido.getStatus() == StatusPedido.EM_PREPARACAO
                && novoStatus == StatusPedido.PRONTO) {

            transicaoValida = true;

// REGRA DE NEGÓCIO - Pedido pronto pode ser entregue.
        } else if (pedido.getStatus() == StatusPedido.PRONTO
                && novoStatus == StatusPedido.ENTREGUE) {

            transicaoValida = true;
        }

        // VALIDAÇÃO - Verifica se a mudança de status é permitida.
        if (!transicaoValida) {

            // TRATAMENTO - Impede transições fora do fluxo operacional.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é permitido alterar o pedido de "
                            + pedido.getStatus()
                            + " para "
                            + novoStatus + "."
            );
        }

// REGRA DE NEGÓCIO - Atualiza somente o status, sem movimentar estoque ou pagamento.
        StatusPedido statusAnterior = pedido.getStatus();
        pedido.setStatus(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

// REGRA DE NEGÓCIO - Identifica o funcionário autenticado pelo JWT.
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        String usuarioId = autenticacao.getName();

// REGRA DE NEGÓCIO - Registra a alteração de status para auditoria.
        logger.info(
                "AUDITORIA PEDIDO | pedidoId={} | usuarioId={} | anterior={} | novo={}",
                pedidoAtualizado.getId(),
                usuarioId,
                statusAnterior,
                novoStatus
        );

        return pedidoAtualizado;
    }
}
