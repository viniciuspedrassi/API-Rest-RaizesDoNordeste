package com.raizesDoNordeste.raizesDoNordeste.repositories;

import com.raizesDoNordeste.raizesDoNordeste.entities.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

}