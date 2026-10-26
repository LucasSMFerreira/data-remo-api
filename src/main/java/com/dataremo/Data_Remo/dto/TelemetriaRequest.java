package com.dataremo.Data_Remo.dto;

public record TelemetriaRequest(String eventoId, String dispositivoId, Long treinoId,
        Long tempoDecorridoMs, Double latitude, Double longitude, Double velocidadeKmh,
        Double aceleracaoX, Double aceleracaoY, Double aceleracaoZ, Boolean simulacao) {}
