package com.raizesDoNordeste.raizesDoNordeste.controllers;


import com.raizesDoNordeste.raizesDoNordeste.entities.Produto;
import com.raizesDoNordeste.raizesDoNordeste.entities.Unidade;
import com.raizesDoNordeste.raizesDoNordeste.services.EstoqueService;
import com.raizesDoNordeste.raizesDoNordeste.services.UnidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;
    private final EstoqueService estoqueService;

    public UnidadeController(UnidadeService unidadeService,
                             EstoqueService estoqueService) {

        this.unidadeService = unidadeService;
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public List<Unidade> listarTodos(){
        return unidadeService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unidade> buscarPorId(@PathVariable Long id) {
        Optional<Unidade> unidade = unidadeService.buscarPorId(id);

        if (unidade.isPresent()) {
            return ResponseEntity.ok(unidade.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Unidade cadastrar (@RequestBody Unidade unidade) {
        return unidadeService.salvar(unidade);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unidade> atualizarUnidade(@PathVariable Long id, @RequestBody Unidade dadosAtualizados) {
        Optional<Unidade> unidade = unidadeService.atualizarUnidade(id, dadosAtualizados);

        if (unidade.isPresent()) {
            return ResponseEntity.ok(unidade.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
// REGRA DE NEGÓCIO - Consulta o cardápio disponível em uma unidade.
    @GetMapping("/{id}/cardapio")
    public ResponseEntity<List<Produto>> consultarCardapio(
            @PathVariable Long id) {

        List<Produto> cardapio = estoqueService.consultarCardapio(id);

        return ResponseEntity.ok(cardapio);
    }

}
