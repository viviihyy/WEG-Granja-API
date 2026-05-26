package com.weg.granja.dto;

import com.weg.granja.model.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ColaboradorDTO {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        private String nome;

        @NotBlank(message = "Matrícula é obrigatória")
        @Size(max = 20, message = "Matrícula deve ter no máximo 20 caracteres")
        private String matricula;

        @NotNull(message = "Turno é obrigatório")
        private Turno turno;

        private boolean temSegundaRefeicao = false;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String nome;
        private String matricula;
        private Turno turno;
        private boolean temSegundaRefeicao;
        private boolean ativo;
    }
}
