package com.raizesDoNordeste.raizesDoNordeste.repositories;

import com.raizesDoNordeste.raizesDoNordeste.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // REGRA DE NEGÓCIO - Permite verificar se o e-mail já está cadastrado.
    boolean existsByEmail(String email);
}