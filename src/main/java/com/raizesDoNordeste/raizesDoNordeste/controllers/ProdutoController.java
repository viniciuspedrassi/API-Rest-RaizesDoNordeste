package com.raizesDoNordeste.raizesDoNordeste.controllers;


import com.raizesDoNordeste.raizesDoNordeste.entities.Produto;
import com.raizesDoNordeste.raizesDoNordeste.services.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/produtos")
@Tag(
        name = "Produtos",
        description = "Operações de cadastro, consulta e atualização de produtos."
)
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(
            summary = "Listar produtos",
            description = "Retorna todos os produtos cadastrados."
    )
    @ApiResponse(responseCode = "200", description = "Produtos consultados com sucesso.")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @Operation(
            summary = "Cadastrar produto",
            description = "Cadastra um novo produto. Operação permitida para administradores e gerentes."
    )
    @ApiResponse(responseCode = "200", description = "Produto cadastrado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos.")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão.")
    @PostMapping
    public Produto cadastrar(@RequestBody Produto produto) {
        return produtoService.salvar(produto);
    }

    @Operation(
            summary = "Buscar produto por ID",
            description = "Consulta um produto específico pelo identificador."
    )
    @ApiResponse(responseCode = "200", description = "Produto encontrado.")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado.")
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id) {

        Optional<Produto> produto = produtoService.buscarPorId(id);

        if (produto.isPresent()) {
            return ResponseEntity.ok(produto.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza os dados de um produto existente. Operação permitida para administradores e gerentes."
    )
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos.")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão.")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado.")
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable Long id,
            @RequestBody Produto dadosAtualizados) {

        Optional<Produto> produto = produtoService.atualizarProduto(id, dadosAtualizados);

        if (produto.isPresent()) {
            return ResponseEntity.ok(produto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
