package com.weg.granja.controller;

import com.weg.granja.dto.ConfiguracaoDiariaDTO;
import com.weg.granja.service.ConfiguracaoDiariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/configuracoes-diarias")
@RequiredArgsConstructor
public class ConfiguracaoDiariaController {

    private final ConfiguracaoDiariaService service;

    @GetMapping
    public ResponseEntity<List<ConfiguracaoDiariaDTO.Response>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<ConfiguracaoDiariaDTO.Response> lista = data != null
            ? service.listarPorData(data)
            : service.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracaoDiariaDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    /**
     * Salva ou atualiza a configuração para uma data+refeição (upsert).
     * Usado pela nutricionista para definir o limite diário de porções.
     */
    @PostMapping
    public ResponseEntity<ConfiguracaoDiariaDTO.Response> salvar(
            @RequestBody @Valid ConfiguracaoDiariaDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracaoDiariaDTO.Response> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ConfiguracaoDiariaDTO.Request dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
