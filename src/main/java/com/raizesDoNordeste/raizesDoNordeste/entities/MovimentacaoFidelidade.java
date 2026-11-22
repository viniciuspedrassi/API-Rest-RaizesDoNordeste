package com.raizesDoNordeste.raizesDoNordeste.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MovimentacaoFidelidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacaoFidelidade tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public MovimentacaoFidelidade() {

    }

    public MovimentacaoFidelidade(Cliente cliente, Pedido pedido,
                                  TipoMovimentacaoFidelidade tipo,
                                  Integer quantidade, LocalDateTime dataHora) {

        this.cliente = cliente;
        this.pedido = pedido;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public TipoMovimentacaoFidelidade getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacaoFidelidade tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }


}