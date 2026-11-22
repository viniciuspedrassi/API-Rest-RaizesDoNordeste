package com.raizesDoNordeste.raizesDoNordeste.repositories;

import com.raizesDoNordeste.raizesDoNordeste.entities.CanalPedido;
import com.raizesDoNordeste.raizesDoNordeste.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByCanalPedido(CanalPedido canalPedido);
}