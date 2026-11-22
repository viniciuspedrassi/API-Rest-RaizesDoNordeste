package com.raizesDoNordeste.raizesDoNordeste.entities;

import com.raizesDoNordeste.raizesDoNordeste.services.EstoqueService;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn (name = "estoque_id", nullable = false)
    private Estoque estoque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column (nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn ( name = "usuario_id", nullable = true)
    private Usuario usuario;

    public MovimentacaoEstoque() {

    }

    public MovimentacaoEstoque(Estoque estoque, TipoMovimentacao tipo,
                               Integer quantidade, LocalDateTime dataHora,
                               Usuario usuario) {

        this.estoque = estoque;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Estoque getEstoque() {
        return estoque;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
