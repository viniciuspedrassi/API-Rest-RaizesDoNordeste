package com.raizesDoNordeste.raizesDoNordeste.repositories;

import com.raizesDoNordeste.raizesDoNordeste.entities.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    List<Estoque> findByUnidadeId(Long unidadeId);
    Optional<Estoque> findByProdutoIdAndUnidadeId ( Long produtoId, Long unidadeId);
}
