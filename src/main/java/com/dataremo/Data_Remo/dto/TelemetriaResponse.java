package com.dataremo.Data_Remo.dto;

import com.dataremo.Data_Remo.model.MedicaoTelemetria;
import java.time.Instant;

public record TelemetriaResponse(String eventoId, String dispositivoId, Long treinoId,
        Long tempoDecorridoMs, Double latitude, Double longitude, Double velocidadeKmh,
        Double aceleracaoX, Double aceleracaoY, Double aceleracaoZ, Instant recebidoEm,
        String origem) {
    public static TelemetriaResponse from(MedicaoTelemetria m) {
        return new TelemetriaResponse(m.eventoId, m.dispositivoId, m.treino.getId(),
                m.tempoDecorridoMs, m.latitude, m.longitude, m.velocidadeKmh,
                m.aceleracaoX, m.aceleracaoY, m.aceleracaoZ, m.recebidoEm, "SIMULACAO");
    }
}
