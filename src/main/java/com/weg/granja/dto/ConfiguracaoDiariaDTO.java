package com.weg.granja.dto;

import com.weg.granja.model.TipoRefeicao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConfiguracaoDiariaDTO {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "Data é obrigatória")
        private LocalDate data;

        @NotNull(message = "Tipo de refeição é obrigatório")
        private TipoRefeicao tipoRefeicao;

        @NotNull(message = "Limite de porções é obrigatório")
        @Min(value = 1, message = "Limite de porções deve ser pelo menos 1")
        private Integer limitePorcoes;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private LocalDate data;
        private TipoRefeicao tipoRefeicao;
        private int limitePorcoes;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }
}
