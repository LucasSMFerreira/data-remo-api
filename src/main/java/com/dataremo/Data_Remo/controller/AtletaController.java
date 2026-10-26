package com.dataremo.Data_Remo.controller;

import com.dataremo.Data_Remo.model.Atleta;
import com.dataremo.Data_Remo.model.Nivel;
import com.dataremo.Data_Remo.repository.AtletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/atletas")
@CrossOrigin
public class AtletaController {

    @Autowired
    private AtletaRepository atletaRepository;

    // lista todos os atletas (usado na recuperação de senha e testes)
    @GetMapping
    public List<Atleta> listar() {
        return atletaRepository.findAll();
    }

    // cadastro
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Atleta atleta) {

        // validações básicas
        if (atleta.getNome() == null || atleta.getNome().isBlank()
                || atleta.getEmail() == null || atleta.getEmail().isBlank()
                || atleta.getSenha() == null || atleta.getSenha().isBlank()) {
            return ResponseEntity.badRequest().body("Nome, email e senha são obrigatórios.");
        }

        // checa duplicidade de nome
        Atleta existentePorNome = atletaRepository.findByNome(atleta.getNome());
        if (existentePorNome != null) {
            return ResponseEntity.status(409).body("Nome já está em uso.");
        }

        // checa duplicidade de email
        Atleta existentePorEmail = atletaRepository.findByEmail(atleta.getEmail());
        if (existentePorEmail != null) {
            return ResponseEntity.status(409).body("Email já está em uso.");
        }

        // valores padrão (evitar null em colunas NOT NULL)
        if (atleta.getPontosTotais() == null) atleta.setPontosTotais(0.0);
        if (atleta.getTotalTreinos() == null) atleta.setTotalTreinos(0);
        if (atleta.getTempoTotalMinutos() == null) atleta.setTempoTotalMinutos(0);
        if (atleta.getNivel() == null) atleta.setNivel(Nivel.INICIANTE_1);

        atleta.setSenha(com.dataremo.Data_Remo.service.SenhaLocal.hash(atleta.getSenha()));
        try {
            Atleta salvo = atletaRepository.save(atleta);
            return ResponseEntity.ok(salvo);
        } catch (DataIntegrityViolationException e) {
            // algum índice/constraint do banco (ex: unique em email) ainda estourou
            e.printStackTrace();
            return ResponseEntity.status(409)
                    .body("Dados inválidos ou duplicados no banco.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao criar atleta.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atleta> buscar(@PathVariable Long id) {
        return atletaRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ranking geral (ordenado por pontos)
    @GetMapping("/ranking")
    public List<Atleta> rankingGeral() {
        return atletaRepository.findAllByOrderByPontosTotaisDesc()
                .stream()
                .filter(a -> a.getPontosTotais() != null && a.getPontosTotais() > 0)
                .toList();
    }

    // login por nome + senha
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String senha = body.get("senha");

        if (nome == null || senha == null) {
            return ResponseEntity.badRequest().body("Nome e senha são obrigatórios.");
        }

        Atleta atleta = atletaRepository.findByNome(nome);

        if (atleta == null || atleta.getSenha() == null || !com.dataremo.Data_Remo.service.SenhaLocal.confere(senha, atleta.getSenha())) {
            return ResponseEntity.status(401).body("Nome ou senha incorretos.");
        }

        return ResponseEntity.ok(atleta);
    }
}