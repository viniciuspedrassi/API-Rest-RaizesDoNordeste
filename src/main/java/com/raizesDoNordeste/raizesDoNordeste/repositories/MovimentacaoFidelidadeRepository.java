package com.raizesDoNordeste.raizesDoNordeste.repositories;


import com.raizesDoNordeste.raizesDoNordeste.entities.MovimentacaoFidelidade;
import org.springframework.data.jpa.repository.JpaRepository;
import com.raizesDoNordeste.raizesDoNordeste.entities.TipoMovimentacaoFidelidade;

public interface MovimentacaoFidelidadeRepository
        extends JpaRepository<MovimentacaoFidelidade, Long> {
    boolean existsByPedidoIdAndTipo(
            Long pedidoId,
            TipoMovimentacaoFidelidade tipo
    );

}