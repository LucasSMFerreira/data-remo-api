package com.dataremo.Data_Remo.service;

import com.dataremo.Data_Remo.dto.*;
import com.dataremo.Data_Remo.model.MedicaoTelemetria;
import com.dataremo.Data_Remo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class TelemetriaService {
    private final TelemetriaRepository medicoes;
    private final TreinoRepository treinos;
    public TelemetriaService(TelemetriaRepository medicoes, TreinoRepository treinos) {
        this.medicoes = medicoes;
        this.treinos = treinos;
    }

    @Transactional
    public TelemetriaResponse receber(TelemetriaRequest r) {
        if (!idValido(r.eventoId()) || !idValido(r.dispositivoId()) || r.treinoId() == null
                || r.treinoId() <= 0 || !Boolean.TRUE.equals(r.simulacao())
                || r.tempoDecorridoMs() == null || r.tempoDecorridoMs() < 0
                || !faixa(r.latitude(), -90, 90) || !faixa(r.longitude(), -180, 180)
                || !faixa(r.velocidadeKmh(), 0, 200)
                || !faixa(r.aceleracaoX(), -200, 200) || !faixa(r.aceleracaoY(), -200, 200)
                || !faixa(r.aceleracaoZ(), -200, 200)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe identificadores, treino e medidas válidas; somente simulacao=true nesta etapa.");
        }
        var existente = medicoes.findById(r.eventoId());
        if (existente.isPresent()) {
            var m = existente.get();
            if (!Objects.equals(m.dispositivoId, r.dispositivoId()) || !Objects.equals(m.treino.getId(), r.treinoId())
                    || !Objects.equals(m.tempoDecorridoMs, r.tempoDecorridoMs())
                    || !Objects.equals(m.latitude, r.latitude()) || !Objects.equals(m.longitude, r.longitude())
                    || !Objects.equals(m.velocidadeKmh, r.velocidadeKmh())
                    || !Objects.equals(m.aceleracaoX, r.aceleracaoX()) || !Objects.equals(m.aceleracaoY, r.aceleracaoY())
                    || !Objects.equals(m.aceleracaoZ, r.aceleracaoZ())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "eventoId já utilizado com outro conteúdo.");
            }
            return TelemetriaResponse.from(m);
        }
        var treino = treinos.findById(r.treinoId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Treino não encontrado."));
        var m = new MedicaoTelemetria();
        m.eventoId = r.eventoId(); m.dispositivoId = r.dispositivoId(); m.treino = treino;
        m.tempoDecorridoMs = r.tempoDecorridoMs(); m.latitude = r.latitude(); m.longitude = r.longitude();
        m.velocidadeKmh = r.velocidadeKmh(); m.aceleracaoX = r.aceleracaoX();
        m.aceleracaoY = r.aceleracaoY(); m.aceleracaoZ = r.aceleracaoZ(); m.recebidoEm = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        return TelemetriaResponse.from(medicoes.saveAndFlush(m));
    }

    @Transactional(readOnly = true)
    public List<TelemetriaResponse> listar(Long treinoId) {
        if (!treinos.existsById(treinoId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Treino não encontrado.");
        return medicoes.findTop50ByTreinoIdOrderByRecebidoEmDescEventoIdAsc(treinoId)
                .stream().map(TelemetriaResponse::from).toList();
    }
    private boolean idValido(String s) { return s != null && s.matches("[A-Za-z0-9_-]{1,64}"); }
    private boolean faixa(Double n, double min, double max) { return n != null && Double.isFinite(n) && n >= min && n <= max; }
}
