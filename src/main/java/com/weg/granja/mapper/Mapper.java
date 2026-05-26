package com.weg.granja.mapper;

import com.weg.granja.dto.ColaboradorDTO;
import com.weg.granja.dto.ConfiguracaoDiariaDTO;
import com.weg.granja.dto.RetiradaDTO;
import com.weg.granja.model.Colaborador;
import com.weg.granja.model.ConfiguracaoDiaria;
import com.weg.granja.model.Retirada;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    // ── Colaborador ─────────────────────────────────────────────────────────

    public Colaborador toColaborador(ColaboradorDTO.Request dto) {
        Colaborador c = new Colaborador();
        c.setNome(dto.getNome());
        c.setMatricula(dto.getMatricula());
        c.setTurno(dto.getTurno());
        c.setTemSegundaRefeicao(dto.isTemSegundaRefeicao());
        return c;
    }

    public ColaboradorDTO.Response toColaboradorResponse(Colaborador c) {
        return new ColaboradorDTO.Response(
            c.getId(),
            c.getNome(),
            c.getMatricula(),
            c.getTurno(),
            c.isTemSegundaRefeicao(),
            c.isAtivo()
        );
    }

    // ── ConfiguracaoDiaria ──────────────────────────────────────────────────

    public ConfiguracaoDiaria toConfiguracao(ConfiguracaoDiariaDTO.Request dto) {
        ConfiguracaoDiaria cfg = new ConfiguracaoDiaria();
        cfg.setData(dto.getData());
        cfg.setTipoRefeicao(dto.getTipoRefeicao());
        cfg.setLimitePorcoes(dto.getLimitePorcoes());
        return cfg;
    }

    public ConfiguracaoDiariaDTO.Response toConfiguracaoResponse(ConfiguracaoDiaria cfg) {
        return new ConfiguracaoDiariaDTO.Response(
            cfg.getId(),
            cfg.getData(),
            cfg.getTipoRefeicao(),
            cfg.getLimitePorcoes(),
            cfg.getCriadoEm(),
            cfg.getAtualizadoEm()
        );
    }

    // ── Retirada ────────────────────────────────────────────────────────────

    public RetiradaDTO.Response toRetiradaResponse(Retirada r) {
        return new RetiradaDTO.Response(
            r.getId(),
            r.getColaborador().getId(),
            r.getColaborador().getNome(),
            r.getColaborador().getMatricula(),
            r.getColaborador().getTurno(),
            r.getDataHora(),
            r.getTipoRefeicao(),
            r.getQuantidade()
        );
    }
}
