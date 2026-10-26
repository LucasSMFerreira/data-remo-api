package com.dataremo.Data_Remo.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atletas")
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Integer idade;
    private Double altura;
    private String modalidade;
    private String objetivo;

    // Armazenamos o plano como texto no banco ("FREE", "PLUS", etc.)
    private String plano;

    private String email;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Nivel nivel = Nivel.INICIANTE_1;

    @Column(nullable = false)
    private Double pontosTotais = 0.0;

    @Column(nullable = false)
    private Integer totalTreinos = 0;

    @Column(nullable = false)
    private Integer tempoTotalMinutos = 0;

    // Badges conquistadas (para o módulo de console/feedback)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atleta_badges", joinColumns = @JoinColumn(name = "atleta_id"))
    @Column(name = "badge")
    private List<String> badges = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonCreator
    public Atleta() {
    }

    // Construtor usado pelo modo console (Main)
    public Atleta(String nome, Integer idade, Double altura,
                  String modalidade, String objetivo, Plano plano) {
        this.nome = nome;
        this.idade = idade;
        this.altura = altura;
        this.modalidade = modalidade;
        this.objetivo = objetivo;
        this.plano = plano.name(); // "FREE", "PLUS", etc.
    }

    // GETTERS / SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getPlano() {
        return plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }

    // helpers para usar o enum Plano quando quiser
    public Plano getPlanoEnum() {
        if (plano == null) return null;
        try {
            return Plano.valueOf(plano);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setPlanoEnum(Plano plano) {
        this.plano = plano != null ? plano.name() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public Double getPontosTotais() {
        return pontosTotais;
    }

    public void setPontosTotais(Double pontosTotais) {
        this.pontosTotais = pontosTotais;
    }

    public Integer getTotalTreinos() {
        return totalTreinos;
    }

    public void setTotalTreinos(Integer totalTreinos) {
        this.totalTreinos = totalTreinos;
    }

    public Integer getTempoTotalMinutos() {
        return tempoTotalMinutos;
    }

    public void setTempoTotalMinutos(Integer tempoTotalMinutos) {
        this.tempoTotalMinutos = tempoTotalMinutos;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    // MÉTODOS AUXILIARES (modo console)

    public void incrementarTreinos() {
        if (this.totalTreinos == null) this.totalTreinos = 0;
        this.totalTreinos++;
    }

    public void adicionarTempo(int minutos) {
        if (this.tempoTotalMinutos == null) this.tempoTotalMinutos = 0;
        this.tempoTotalMinutos += minutos;
    }

    public void adicionarPontos(double pontos) {
        if (this.pontosTotais == null) this.pontosTotais = 0.0;
        this.pontosTotais += pontos;
    }
}