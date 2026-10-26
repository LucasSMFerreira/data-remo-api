package com.dataremo.Data_Remo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "medicoes_telemetria")
public class MedicaoTelemetria {
    @Id @Column(length = 64)
    public String eventoId;
    @Column(nullable = false, length = 64)
    public String dispositivoId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false)
    public Treino treino;
    @Column(nullable = false)
    public Long tempoDecorridoMs;
    public Double latitude;
    public Double longitude;
    public Double velocidadeKmh;
    public Double aceleracaoX;
    public Double aceleracaoY;
    public Double aceleracaoZ;
    @Column(nullable = false)
    public Instant recebidoEm;
}
