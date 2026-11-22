package com.raizesDoNordeste.raizesDoNordeste.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Cliente {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String senhaHash;
    @Column(nullable = false)
    private Integer pontosFidelidade = 0;

    @Column(nullable = false)
    private Boolean consentimentoFidelidade = false;

    @Column
    private LocalDateTime dataConsentimentoFidelidade;

    public Cliente() {

    }

    public Cliente(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Boolean getConsentimentoFidelidade() {
        return consentimentoFidelidade;
    }

    public void setConsentimentoFidelidade(Boolean consentimentoFidelidade) {
        this.consentimentoFidelidade = consentimentoFidelidade;
    }

    public Integer getPontosFidelidade() {
        return pontosFidelidade;
    }

    public void setPontosFidelidade(Integer pontosFidelidade) {
        this.pontosFidelidade = pontosFidelidade;
    }

    public LocalDateTime getDataConsentimentoFidelidade() {
        return dataConsentimentoFidelidade;
    }

    public void setDataConsentimentoFidelidade(LocalDateTime dataConsentimentoFidelidade) {
        this.dataConsentimentoFidelidade = dataConsentimentoFidelidade;
    }




}
