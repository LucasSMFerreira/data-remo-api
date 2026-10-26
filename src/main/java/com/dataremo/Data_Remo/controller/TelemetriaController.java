package com.dataremo.Data_Remo.controller;

import com.dataremo.Data_Remo.dto.*;
import com.dataremo.Data_Remo.service.TelemetriaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@RestController
@RequestMapping("/api/telemetria")
public class TelemetriaController {
    private final TelemetriaService service;
    public TelemetriaController(TelemetriaService service) { this.service = service; }
    @PostMapping
    public TelemetriaResponse receber(@RequestBody TelemetriaRequest request) {
        try { return service.receber(request); }
        catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflito de evento; reenvie o mesmo pacote para confirmar.");
        }
    }
    @GetMapping("/evento/{id}")
    public TelemetriaResponse buscar(@PathVariable String id) { return service.buscar(id); }
    @GetMapping("/treino/{id}")
    public List<TelemetriaResponse> listar(@PathVariable Long id) { return service.listar(id); }
}
