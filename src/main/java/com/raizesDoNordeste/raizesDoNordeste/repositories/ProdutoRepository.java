package com.raizesDoNordeste.raizesDoNordeste.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.raizesDoNordeste.raizesDoNordeste.entities.Produto;

public interface ProdutoRepository  extends JpaRepository<Produto, Long>  {
}
