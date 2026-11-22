package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.entities.Pedido;
import com.raizesDoNordeste.raizesDoNordeste.entities.StatusPagamento;
import com.raizesDoNordeste.raizesDoNordeste.entities.StatusPedido;
import com.raizesDoNordeste.raizesDoNordeste.repositories.PagamentoRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.raizesDoNordeste.raizesDoNordeste.entities.Cliente;
import com.raizesDoNordeste.raizesDoNordeste.entities.MovimentacaoFidelidade;
import com.raizesDoNordeste.raizesDoNordeste.entities.TipoMovimentacaoFidelidade;
import com.raizesDoNordeste.raizesDoNordeste.repositories.ClienteRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.MovimentacaoFidelidadeRepository;

import com.raizesDoNordeste.raizesDoNordeste.entities.ItemPedido;
import com.raizesDoNordeste.raizesDoNordeste.repositories.ItemPedidoRepository;
import com.raizesDoNordeste.raizesDoNordeste.entities.Pagamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EstoqueService estoqueService;
    private final ClienteRepository clienteRepository;
    private final MovimentacaoFidelidadeRepository movimentacaoFidelidadeRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            PedidoRepository pedidoRepository,
                            ItemPedidoRepository itemPedidoRepository,
                            EstoqueService estoqueService,
                            ClienteRepository clienteRepository,
                            MovimentacaoFidelidadeRepository movimentacaoFidelidadeRepository) {

        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.estoqueService = estoqueService;
        this.clienteRepository = clienteRepository;
        this.movimentacaoFidelidadeRepository = movimentacaoFidelidadeRepository;
    }

// REGRA DE NEGÓCIO - Somente pedidos aguardando pagamento podem iniciar o processamento de um pagamento.
    @Transactional
    public Pedido simularPagamento(Long pedidoId, Boolean aprovado, Integer pontosResgatar) {
        // VALIDAÇÃO - Verifica se o ID do pedido foi informado.
            if (pedidoId == null) {

                // TRATAMENTO - Rejeita pagamentos sem pedido identificados.
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O ID do pedido deve ser informado."
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

        // VALIDAÇÃO - Verifica se o pedido está aguardando pagamento.
                if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {

                    // TRATAMENTO - Impede o processamento de pedidos que não estão aguardando pagamento.
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "O pedido não está aguardando pagamento."
                    );
                }

//////////////////////////////////////////
        List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(pedidoId);

        // VALIDAÇÃO - Verifica se o pedido possui itens registrados.
            if (itens.isEmpty()) {

                // TRATAMENTO - Impede pagamentos de pedidos sem produtos.
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O pedido não possui itens para pagamento."
                );
            }

// REGRA DE NEGÓCIO - Calcula o valor do pagamento com os preços registrados no momento da criação do pedido.
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedido item : itens) {

            BigDecimal subtotal = item.getPrecoUnitario().multiply(
                    BigDecimal.valueOf(item.getQuantidade())
            );

            valorTotal = valorTotal.add(subtotal);
        }

        // VALIDAÇÃO - Verifica se o valor total do pedido é positivo.
                if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {

                    // TRATAMENTO - Rejeita pagamentos com valor inválido.
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "O valor total do pedido deve ser maior que zero."
                    );
                }

        // VALIDAÇÃO - Verifica se o resultado simulado do pagamento foi informado.
        if (aprovado == null) {

            // TRATAMENTO - Rejeita pagamentos sem resultado.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O resultado do pagamento deve ser informado."
            );
        }

// REGRA DE NEGÓCIO - Cada pedido pode possuir somente um pagamento.

        // VALIDAÇÃO - Verifica se já existe pagamento registrado para o pedido.
                if (pagamentoRepository.existsByPedidoId(pedidoId)) {

                    // TRATAMENTO - Impede o registro de pagamentos duplicados.
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Já existe um pagamento registrado para este pedido."
                    );
                }
// REGRA DE NEGÓCIO - O resgate é opcional e deve ocorrer em múltiplos de 10 pontos.
        if (pontosResgatar == null) {
            pontosResgatar = 0;
        }

        // VALIDAÇÃO - Verifica se a quantidade de pontos solicitada é válida.
                if (pontosResgatar < 0 || pontosResgatar % 10 != 0) {

                    // TRATAMENTO - Rejeita quantidades negativas ou fora da regra de conversão.
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "O resgate deve ser realizado em múltiplos de 10 pontos."
                    );
                }

        Cliente clienteResgate = pedido.getCliente();

        if (pontosResgatar > 0) {

            // VALIDAÇÃO - Verifica se o pedido possui um cliente identificado.
            if (clienteResgate == null) {

                // TRATAMENTO - Impede resgates em pedidos sem cliente.
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O pedido precisa possuir um cliente para resgatar pontos."
                );
            }

            // VALIDAÇÃO - Verifica se o cliente autorizou participar da fidelização.
            if (!Boolean.TRUE.equals(clienteResgate.getConsentimentoFidelidade())) {

                // TRATAMENTO - Impede o resgate sem consentimento ativo.
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O cliente não possui consentimento ativo para fidelização."
                );
            }

            // VALIDAÇÃO - Verifica se o cliente possui pontos suficientes.
            if (clienteResgate.getPontosFidelidade() < pontosResgatar) {

                // TRATAMENTO - Impede resgates acima do saldo disponível.
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Saldo de pontos insuficiente para o resgate."
                );
            }
        }

// REGRA DE NEGÓCIO - Cada 10 pontos resgatados concedem R$ 1,00 de desconto.
        BigDecimal descontoFidelidade = BigDecimal.valueOf(pontosResgatar)
                .divide(BigDecimal.TEN);

        BigDecimal valorPagamento = valorTotal.subtract(descontoFidelidade);

            // VALIDAÇÃO - Verifica se o desconto mantém um valor positivo para pagamento.
                    if (valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

                        // TRATAMENTO - Impede descontos iguais ou superiores ao valor da compra.
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "O desconto não pode atingir ou ultrapassar o valor da compra."
                        );
                    }
        StatusPagamento statusPagamento;

        if (aprovado) {
            statusPagamento = StatusPagamento.APROVADO;
        } else {
            statusPagamento = StatusPagamento.RECUSADO;
        }

// REGRA DE NEGÓCIO - O mock simula a aprovação ou recusa do pagamento.
        if (aprovado) {
            statusPagamento = StatusPagamento.APROVADO;
        } else {
            statusPagamento = StatusPagamento.RECUSADO;
        }

// REGRA DE NEGÓCIO - Registra os dados enviados e recebidos do gateway simulado.
        String requisicaoGateway =
                "{\"pedidoId\":" + pedidoId +
                        ",\"valor\":" + valorPagamento + "}";

        String respostaGateway =
                "{\"aprovado\": " + aprovado +
                        ",\"status\": \"" + statusPagamento + "\"}";

// REGRA DE NEGÓCIO - Pagamentos aprovados autorizam a saída dos produtos do estoque da unidade.
        if (aprovado) {

            for (ItemPedido item : itens) {

                estoqueService.registrarSaidaEstoque(
                        item.getProduto().getId(),
                        pedido.getUnidade().getId(),
                        item.getQuantidade()
                );
            }

            pedido.setStatus(StatusPedido.RECEBIDO);

// REGRA DE NEGÓCIO - Efetiva o resgate somente após a aprovação do pagamento.
            if (pontosResgatar > 0) {

// REGRA DE NEGÓCIO - Desconta do saldo os pontos utilizados na compra.
                clienteResgate.setPontosFidelidade(
                        clienteResgate.getPontosFidelidade() - pontosResgatar
                );

                clienteRepository.save(clienteResgate);

// REGRA DE NEGÓCIO - Registra o resgate vinculado ao pedido.
                MovimentacaoFidelidade movimentacaoResgate =
                        new MovimentacaoFidelidade(
                                clienteResgate,
                                pedido,
                                TipoMovimentacaoFidelidade.RESGATE,
                                pontosResgatar,
                                LocalDateTime.now()
                        );

                movimentacaoFidelidadeRepository.save(movimentacaoResgate);
            }

// REGRA DE NEGÓCIO - Concede pontos somente para compras aprovadas de clientes com consentimento.
            Cliente cliente = pedido.getCliente();

            if (cliente != null
                    && Boolean.TRUE.equals(cliente.getConsentimentoFidelidade())) {

                // VALIDAÇÃO - Verifica se o pedido já recebeu pontos.
                if (movimentacaoFidelidadeRepository.existsByPedidoIdAndTipo(
                        pedidoId, TipoMovimentacaoFidelidade.CREDITO)) {

                    // TRATAMENTO - Impede créditos duplicados para a mesma compra.
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este pedido já recebeu pontos de fidelidade."
                    );
                }

                int pontos = valorPagamento.toBigInteger().intValueExact();

// REGRA DE NEGÓCIO - Acrescenta ao saldo os pontos gerados pela compra.
                if (pontos > 0) {

                    cliente.setPontosFidelidade(
                            cliente.getPontosFidelidade() + pontos
                    );

                    clienteRepository.save(cliente);

// REGRA DE NEGÓCIO - Registra o crédito vinculado ao pedido.
                    MovimentacaoFidelidade movimentacao =
                            new MovimentacaoFidelidade(
                                    cliente,
                                    pedido,
                                    TipoMovimentacaoFidelidade.CREDITO,
                                    pontos,
                                    LocalDateTime.now()
                            );

                    movimentacaoFidelidadeRepository.save(movimentacao);
                }
            }

        } else {

// REGRA DE NEGÓCIO - Pagamentos recusados cancelam o pedido sem descontar produtos do estoque.
            pedido.setStatus(StatusPedido.CANCELADO);
        }

        Pagamento pagamento = new Pagamento(
                pedido,
                valorPagamento,
                statusPagamento,
                LocalDateTime.now(),
                requisicaoGateway,
                respostaGateway
        );

        pagamentoRepository.save(pagamento);

        pedidoRepository.save(pedido);

        return pedido;


    }
}