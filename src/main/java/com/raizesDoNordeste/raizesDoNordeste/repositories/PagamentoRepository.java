package com.raizesDoNordeste.raizesDoNordeste.repositories;
import com.raizesDoNordeste.raizesDoNordeste.entities.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    boolean existsByPedidoId(Long pedidoId);
}