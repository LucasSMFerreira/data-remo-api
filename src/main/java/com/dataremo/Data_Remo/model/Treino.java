package com.dataremo.Data_Remo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "treinos")
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relação com Atleta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Atleta atleta;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false)
    private Double distanciaKm;

    @Column(nullable = false)
    private String intensidade; // LEVE, MODERADA, INTENSA

    @Column(nullable = false)
    private String tipo;        // BASE, INTERVALADO, PROVA

    @Column(length = 500)
    private String notas;

    // Pontos calculados para este treino
    @Column(nullable = false)
    private Double pontosTreino;

    public Treino() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Atleta getAtleta() {
        return atleta;
    }

    public void setAtleta(Atleta atleta) {
        this.atleta = atleta;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public String getIntensidade() {
        return intensidade;
    }

    public void setIntensidade(String intensidade) {
        this.intensidade = intensidade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Double getPontosTreino() {
        return pontosTreino;
    }

    public void setPontosTreino(Double pontosTreino) {
        this.pontosTreino = pontosTreino;
    }
}