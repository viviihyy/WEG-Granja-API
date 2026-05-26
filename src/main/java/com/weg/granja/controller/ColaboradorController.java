package com.weg.granja.controller;

import com.weg.granja.dto.ColaboradorDTO;
import com.weg.granja.service.ColaboradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colaboradores")
@RequiredArgsConstructor
public class ColaboradorController {

    private final ColaboradorService service;

    @GetMapping
    public ResponseEntity<List<ColaboradorDTO.Response>> listar(
            @RequestParam(defaultValue = "true") boolean apenasAtivos) {
        List<ColaboradorDTO.Response> lista = apenasAtivos ? service.listarAtivos() : service.listarTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColaboradorDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<ColaboradorDTO.Response> buscarPorMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(service.buscarPorMatricula(matricula));
    }

    @PostMapping
    public ResponseEntity<ColaboradorDTO.Response> cadastrar(@RequestBody @Valid ColaboradorDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColaboradorDTO.Response> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ColaboradorDTO.Request dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
