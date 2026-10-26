package com.dataremo.Data_Remo.service;

import com.dataremo.Data_Remo.dto.TreinoRequest;
import com.dataremo.Data_Remo.model.Atleta;
import com.dataremo.Data_Remo.model.Nivel;
import com.dataremo.Data_Remo.model.Treino;
import com.dataremo.Data_Remo.repository.AtletaRepository;
import com.dataremo.Data_Remo.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final AtletaRepository atletaRepository;

    public TreinoService(TreinoRepository treinoRepository, AtletaRepository atletaRepository) {
        this.treinoRepository = treinoRepository;
        this.atletaRepository = atletaRepository;
    }

    @Transactional(readOnly = true)
    public java.util.List<Treino> historico(Long id) {
        return treinoRepository.findByAtletaIdOrderByDataDescIdDesc(id);
    }

    @Transactional
    public Treino registrarTreino(TreinoRequest req) {
        if (req.getAtletaId() == null || req.getData() == null || req.getDuracaoMinutos() == null || req.getDuracaoMinutos() <= 0 || req.getDistanciaKm() == null || !Double.isFinite(req.getDistanciaKm()) || req.getDistanciaKm() <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Informe data, duracao e distancia positivas");
        }
        Atleta atleta = atletaRepository.findById(req.getAtletaId())
                .orElseThrow(() -> new IllegalArgumentException("Atleta não encontrado"));

        double pontos = calcularPontos(req);

        Treino treino = new Treino();
        treino.setAtleta(atleta);
        treino.setData(req.getData());
        treino.setDuracaoMinutos(req.getDuracaoMinutos());
        treino.setDistanciaKm(req.getDistanciaKm());
        treino.setIntensidade(req.getIntensidade());
        treino.setTipo(req.getTipo());
        treino.setNotas(req.getNotas());
        treino.setPontosTreino(pontos);

        treinoRepository.save(treino);

        // Atualiza estatísticas agregadas do atleta
        double ptsAtuais = atleta.getPontosTotais() != null ? atleta.getPontosTotais() : 0.0;
        int treinosAtuais = atleta.getTotalTreinos() != null ? atleta.getTotalTreinos() : 0;
        int tempoAtual = atleta.getTempoTotalMinutos() != null ? atleta.getTempoTotalMinutos() : 0;

        atleta.setPontosTotais(ptsAtuais + pontos);
        atleta.setTotalTreinos(treinosAtuais + 1);

        if (req.getDuracaoMinutos() != null) {
            atleta.setTempoTotalMinutos(tempoAtual + req.getDuracaoMinutos());
        }

        atualizarNivel(atleta);

        atletaRepository.save(atleta);

        return treino;
    }

    private double calcularPontos(TreinoRequest req) {
        double base = 0.0;

        // 1 ponto por minuto
        if (req.getDuracaoMinutos() != null) {
            base += req.getDuracaoMinutos();
        }

        // 5 pontos por km
        if (req.getDistanciaKm() != null) {
            base += req.getDistanciaKm() * 5.0;
        }

        // Multiplicador por intensidade
        String intensidade = (req.getIntensidade() != null ? req.getIntensidade() : "")
                .toUpperCase(Locale.ROOT);
        double multIntensidade = switch (intensidade) {
            case "INTENSA"   -> 1.5;
            case "MODERADA"  -> 1.2;
            default          -> 1.0; // LEVE ou vazio
        };

        // Multiplicador por tipo
        String tipo = (req.getTipo() != null ? req.getTipo() : "")
                .toUpperCase(Locale.ROOT);
        double multTipo = switch (tipo) {
            case "PROVA"        -> 1.3;
            case "INTERVALADO"  -> 1.1;
            default             -> 1.0; // BASE
        };

        return base * multIntensidade * multTipo;
    }

    private void atualizarNivel(Atleta atleta) {
        double pontos = atleta.getPontosTotais() != null ? atleta.getPontosTotais() : 0.0;
        int treinos = atleta.getTotalTreinos() != null ? atleta.getTotalTreinos() : 0;
        int tempo = atleta.getTempoTotalMinutos() != null ? atleta.getTempoTotalMinutos() : 0;

        Nivel melhorNivel = Nivel.INICIANTE_1;

        for (Nivel nivelPossivel : Nivel.values()) {
            boolean temPontos = pontos >= nivelPossivel.getPontosParaSubir();
            boolean temTreinos = treinos >= nivelPossivel.getTreinosMinimos();
            boolean temTempo = tempo >= nivelPossivel.getTempoMinimoAcum();

            if (temPontos && temTreinos && temTempo) {
                melhorNivel = nivelPossivel;
            }
        }

        atleta.setNivel(melhorNivel);
    }
}