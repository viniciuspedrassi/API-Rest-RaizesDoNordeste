package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.entities.Produto;
import com.raizesDoNordeste.raizesDoNordeste.repositories.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto salvar(Produto produto) {

//TRATAMENTO  - Impede o cadastro de produtos com nome vazio ou nulo.

        if (produto.getNome() == null || produto.getNome().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do produto não pode ser vazio."
            );
        }

//TRATAMENTO  - Impede o cadastro de produtos que não sejam superiores a zero.

        if (produto.getPreco() == null ||
                produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O preço deve ser maior que zero."
            );
        }

        return produtoRepository.save(produto);
    }
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }
    public Optional<Produto> atualizarProduto(Long id, Produto dadosAtualizados) {

        Optional<Produto> produtoEncontrado = produtoRepository.findById(id);

        if (produtoEncontrado.isPresent()) {

//TRATAMENTO  - Impede que na atualização o nome do produto seja vazio ou nulo.

            if (dadosAtualizados.getNome() == null ||
                    dadosAtualizados.getNome().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O nome do produto não pode ser vazio."
                );
            }

            Produto produto = produtoEncontrado.get();

//TRATAMENTO  - Impede que na atualização os valores não sejam superiores a zero.

            if (dadosAtualizados.getPreco() == null ||
                    dadosAtualizados.getPreco().compareTo(BigDecimal.ZERO) <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O preço deve ser maior que zero."
                );
            }

            produto.setNome(dadosAtualizados.getNome());
            produto.setDescricao(dadosAtualizados.getDescricao());
            produto.setPreco(dadosAtualizados.getPreco());

            produtoRepository.save(produto);

            return Optional.of(produto);

        } else {
            return Optional.empty();
        }
    }

}
