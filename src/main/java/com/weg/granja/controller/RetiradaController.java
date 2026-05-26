package com.weg.granja.controller;

import com.weg.granja.dto.RetiradaDTO;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.service.RetiradaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/retiradas")
@RequiredArgsConstructor
public class RetiradaController {

    private final RetiradaService service;

    /**
     * Histórico de retiradas, filtrado por data (padrão: hoje).
     * GET /retiradas?data=2025-06-10
     */
    @GetMapping
    public ResponseEntity<List<RetiradaDTO.Response>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) TipoRefeicao tipoRefeicao) {
        List<RetiradaDTO.Response> lista = tipoRefeicao != null
            ? service.listarPorDataERefeicao(data, tipoRefeicao)
            : service.listarHistorico(data);
        return ResponseEntity.ok(lista);
    }

    /**
     * Histórico de retiradas de um colaborador específico.
     * GET /retiradas/colaborador/{id}
     */
    @GetMapping("/colaborador/{id}")
    public ResponseEntity<List<RetiradaDTO.Response>> listarPorColaborador(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarPorColaborador(id));
    }
}
