package com.weg.granja.service;

import com.weg.granja.dto.RetiradaDTO;
import com.weg.granja.mapper.Mapper;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.repository.RetiradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetiradaService {

    private final RetiradaRepository repository;
    private final Mapper mapper;

    @Transactional(readOnly = true)
    public List<RetiradaDTO.Response> listarHistorico(LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now();
        return repository.findHistorico(dia.atStartOfDay(), dia.atTime(23, 59, 59))
            .stream()
            .map(mapper::toRetiradaResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RetiradaDTO.Response> listarPorColaborador(Long colaboradorId) {
        return repository.findByColaboradorId(colaboradorId)
            .stream()
            .map(mapper::toRetiradaResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RetiradaDTO.Response> listarPorDataERefeicao(LocalDate data, TipoRefeicao tipoRefeicao) {
        LocalDate dia = data != null ? data : LocalDate.now();
        return repository.findByDataERefeicao(dia.atStartOfDay(), dia.atTime(23, 59, 59), tipoRefeicao)
            .stream()
            .map(mapper::toRetiradaResponse)
            .toList();
    }
}
