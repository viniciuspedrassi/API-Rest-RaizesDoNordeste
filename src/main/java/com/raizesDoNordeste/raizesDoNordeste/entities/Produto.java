package com.raizesDoNordeste.raizesDoNordeste.entities;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Produto {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

public Produto (){

};

public Produto (String nome, String descricao, BigDecimal preco){
    this.nome = nome;
    this.descricao = descricao;
    this.preco = preco;
}

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
