package com.raizesDoNordeste.raizesDoNordeste.services;
import com.raizesDoNordeste.raizesDoNordeste.entities.*;
import com.raizesDoNordeste.raizesDoNordeste.repositories.EstoqueRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.MovimentacaoEstoqueRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.ProdutoRepository;
import com.raizesDoNordeste.raizesDoNordeste.repositories.UnidadeRepository;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final UnidadeRepository unidadeRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;


    public EstoqueService(EstoqueRepository estoqueRepository,
                          ProdutoRepository produtoRepository,
                          UnidadeRepository unidadeRepository,
                          MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {

        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
        this.unidadeRepository = unidadeRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    public Optional<Estoque> buscarPorProdutoEUnidade(Long produtoId, Long unidadeId) {

        return estoqueRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId);
    }
// REGRA DE NEGÓCIO - Garante que a atualização de estoque e o registro de movimentação ocorram na mesma transação.
    @Transactional
    public Estoque registrarEntradaEstoque(Long produtoId, Long unidadeId, Integer quantidade ){
        // VALIDAÇÃO - Verifica se a quantidade de entrada é maior que zero.
            if ( quantidade == null || quantidade <= 0 ) {
                // TRATAMENTO - Rejeita quantidades inválidas com HTTP 400.
                        throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "A quantidade de entrada deve ser maior que zero.");                    }

            Optional<Produto> produtoEncontrado = produtoRepository.findById(produtoId);

        // VALIDAÇÃO - Verifica se o produto existe.
            if (produtoEncontrado.isEmpty()){
                // TRATAMENTO - Retorna HTTP 404 para produto inexistente.
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Produto não encontrado."
                        );
                    }
            Optional<Unidade> unidadeEncontrada = unidadeRepository.findById(unidadeId);

        // VALIDAÇÃO - Verifica se a unidade existe.
                    if (unidadeEncontrada.isEmpty()) {
                // TRATAMENTO - Retorna HTTP 404 para unidade inexistente.
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Unidade não encontrada."
                        );
                    }
            Optional<Estoque> estoqueEncontrado = estoqueRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId);

// REGRA DE NEGÓCIO - Atualiza estoque existente ou cria um novo registro
                Estoque estoque;

        // VALIDAÇÃO - Verifica se há estoque e soma quantidade recebida no estoque atual. Se não, cria o primeiro registro do produto na unidade.
                    if (estoqueEncontrado.isPresent()) {
                        estoque = estoqueEncontrado.get();
                        estoque.setQuantidade(estoque.getQuantidade() + quantidade);

                    } else {
                        estoque = new Estoque(
                                produtoEncontrado.get(),
                                unidadeEncontrada.get(),
                                quantidade
                        );
                    }

                estoque = estoqueRepository.save(estoque);

// REGRA DE NEGÓCIO - Toda entrada de produtos devem ser registradas para manter o histórico do estoque.
                MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                        estoque,
                        TipoMovimentacao.ENTRADA,
                        quantidade,
                        LocalDateTime.now(),
                        null
                );

                movimentacaoEstoqueRepository.save(movimentacao);

                return estoque;

                }
// REGRA DE NEGÓCIO - Permite a saída de proddutos somente quando tiver saldo suficiente, descontando a quantidade solicitada e registranda essa saída.
    @Transactional
    public Estoque registrarSaidaEstoque(Long produtoId, Long unidadeId, Integer quantidade) {

        // VALIDAÇÃO - Verifica se quantidade de saída é maior que zero.
        if (quantidade == null || quantidade <= 0) {
            // TRATAMENTO - Rejeita a operação com HTTP 400.
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A quantidade de saída deve ser maior que zero."
                );
        }
            Optional<Estoque> estoqueEncontrado = estoqueRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId);

        // VALIDAÇÃO - Verifica se existe estoque do produto na unidade.
        if (estoqueEncontrado.isEmpty()) {
            // TRATAMENTO - Retorna HTTP 404 quando não existe registro de estoque.
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Estoque não encontrado para este produto e unidade."
                    );
                }
        Estoque estoque = estoqueEncontrado.get();

// REGRA DE NEGÓCIO - Impede saídas superiores ao saldo disponível.

        // VALIDAÇÃO - Verifica se a quantidade solicitada ultrapassa o estoque.
                if (quantidade > estoque.getQuantidade()) {
            // TRATAMENTO - Retorna HTTP 409 quando o estoque é insuficiente.
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Estoque insuficiente para realizar a saída."
                    );
                }
// REGRA DE NEGÓCIO - Desconta a quantidade retirada do estoque disponível.
        estoque.setQuantidade(estoque.getQuantidade() - quantidade);

        estoque = estoqueRepository.save(estoque);

// REGRA DE NEGÓCIO - Registra a saída no histórico de movimentações.
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                estoque,
                TipoMovimentacao.SAIDA,
                quantidade,
                LocalDateTime.now(),
                null
        );

        movimentacaoEstoqueRepository.save(movimentacao);

        return estoque;
    }

// REGRA DE NEGÓCIO - Retorna os produtos disponíveis no cardápio da unidade.
    public List<Produto> consultarCardapio(Long unidadeId) {

        // VALIDAÇÃO - Verifica se a unidade foi informada.
        if (unidadeId == null) {
            // TRATAMENTO - Rejeita a consulta sem unidade.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A unidade deve ser informada."
            );
        }

        // VALIDAÇÃO - Verifica se a unidade existe.
        if (!unidadeRepository.existsById(unidadeId)) {
            // TRATAMENTO - Retorna 404 para unidade inexistente.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unidade não encontrada."
            );
        }

        List<Estoque> estoques = estoqueRepository.findByUnidadeId(unidadeId);
        List<Produto> cardapio = new ArrayList<>();

// REGRA DE NEGÓCIO - Exibe apenas produtos com estoque maior que zero.
        for (Estoque estoque : estoques) {
            if (estoque.getQuantidade() != null
                    && estoque.getQuantidade() > 0) {

                cardapio.add(estoque.getProduto());
            }
        }

        return cardapio;
    }
}