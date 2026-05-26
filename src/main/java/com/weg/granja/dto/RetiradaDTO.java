package com.weg.granja.dto;

import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.model.Turno;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class RetiradaDTO {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long colaboradorId;
        private String colaboradorNome;
        private String colaboradorMatricula;
        private Turno colaboradorTurno;
        private LocalDateTime dataHora;
        private TipoRefeicao tipoRefeicao;
        private int quantidade;
    }
}
