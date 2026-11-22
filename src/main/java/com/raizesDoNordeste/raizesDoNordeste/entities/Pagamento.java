package com.raizesDoNordeste.raizesDoNordeste.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSON")
    private String requisicaoGateway;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String respostaGateway;

    public Pagamento (){

    }

    public Pagamento(Pedido pedido, BigDecimal valor,
                     StatusPagamento status, LocalDateTime dataHora,
                     String requisicaoGateway, String respostaGateway) {

        this.pedido = pedido;
        this.valor = valor;
        this.status = status;
        this.dataHora = dataHora;
        this.requisicaoGateway = requisicaoGateway;
        this.respostaGateway = respostaGateway;
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public String getRequisicaoGateway() {
        return requisicaoGateway;
    }

    public void setRequisicaoGateway(String requisicaoGateway) {
        this.requisicaoGateway = requisicaoGateway;
    }

    public String getRespostaGateway() {
        return respostaGateway;
    }

    public void setRespostaGateway(String respostaGateway) {
        this.respostaGateway = respostaGateway;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}