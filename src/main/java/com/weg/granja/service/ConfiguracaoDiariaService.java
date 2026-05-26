package com.weg.granja.service;

import com.weg.granja.dto.ConfiguracaoDiariaDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.mapper.Mapper;
import com.weg.granja.model.ConfiguracaoDiaria;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.repository.ConfiguracaoDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracaoDiariaService {

    private final ConfiguracaoDiariaRepository repository;
    private final Mapper mapper;

    @Transactional(readOnly = true)
    public List<ConfiguracaoDiariaDTO.Response> listarTodas() {
        return repository.findAllByOrderByDataDescTipoRefeicaoAsc()
            .stream()
            .map(mapper::toConfiguracaoResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConfiguracaoDiariaDTO.Response> listarPorData(LocalDate data) {
        return repository.findByDataOrderByTipoRefeicao(data)
            .stream()
            .map(mapper::toConfiguracaoResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracaoDiariaDTO.Response buscarPorId(Long id) {
        return mapper.toConfiguracaoResponse(encontrarPorId(id));
    }

    @Transactional
    public ConfiguracaoDiariaDTO.Response salvar(ConfiguracaoDiariaDTO.Request dto) {
        // Upsert: se já existe config para esta data+refeição, atualiza o limite
        ConfiguracaoDiaria cfg = repository
            .findByDataAndTipoRefeicao(dto.getData(), dto.getTipoRefeicao())
            .orElse(mapper.toConfiguracao(dto));

        cfg.setLimitePorcoes(dto.getLimitePorcoes());
        cfg.setData(dto.getData());
        cfg.setTipoRefeicao(dto.getTipoRefeicao());

        return mapper.toConfiguracaoResponse(repository.save(cfg));
    }

    @Transactional
    public ConfiguracaoDiariaDTO.Response atualizar(Long id, ConfiguracaoDiariaDTO.Request dto) {
        ConfiguracaoDiaria cfg = encontrarPorId(id);

        // Valida conflito de data+refeição com outro registro
        repository.findByDataAndTipoRefeicao(dto.getData(), dto.getTipoRefeicao())
            .ifPresent(outro -> {
                if (!outro.getId().equals(id)) {
                    throw new NegocioException(
                        "Já existe configuração para " + dto.getData() + " / " + dto.getTipoRefeicao() + "."
                    );
                }
            });

        cfg.setData(dto.getData());
        cfg.setTipoRefeicao(dto.getTipoRefeicao());
        cfg.setLimitePorcoes(dto.getLimitePorcoes());
        return mapper.toConfiguracaoResponse(repository.save(cfg));
    }

    @Transactional
    public void deletar(Long id) {
        repository.delete(encontrarPorId(id));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    public ConfiguracaoDiaria encontrarPorDataERefeicao(LocalDate data, TipoRefeicao tipoRefeicao) {
        return repository.findByDataAndTipoRefeicao(data, tipoRefeicao)
            .orElseThrow(() -> new NegocioException(
                "Nenhuma configuração cadastrada para " + data + " / " + tipoRefeicao +
                ". Solicite à nutricionista que defina o limite do dia.",
                HttpStatus.NOT_FOUND
            ));
    }

    private ConfiguracaoDiaria encontrarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NegocioException(
                "Configuração com id " + id + " não encontrada.",
                HttpStatus.NOT_FOUND
            ));
    }
}
