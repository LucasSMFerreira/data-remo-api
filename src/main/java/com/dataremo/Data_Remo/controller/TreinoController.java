package com.dataremo.Data_Remo.controller;

import com.dataremo.Data_Remo.dto.TreinoRequest;
import com.dataremo.Data_Remo.model.Treino;
import com.dataremo.Data_Remo.service.TreinoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treinos")
@CrossOrigin // opcional, já que você tem CorsConfig global
public class TreinoController {

    private final TreinoService treinoService;

    public TreinoController(TreinoService treinoService) {
        this.treinoService = treinoService;
    }

    @GetMapping("/atleta/{id}")
    public java.util.List<Treino> historico(@PathVariable Long id) { return treinoService.historico(id); }

    @PostMapping
    public ResponseEntity<Treino> registrar(@RequestBody TreinoRequest request) {
        Treino salvo = treinoService.registrarTreino(request);
        return ResponseEntity.ok(salvo);
    }
}