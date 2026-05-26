package com.weg.granja.dto;

import com.weg.granja.model.TipoRefeicao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DispenserResponseDTO {
    private String status;           // LIBERADO | BLOQUEADO
    private String mensagem;
    private String colaboradorNome;
    private String matricula;
    private TipoRefeicao refeicao;
    private int porcoesUtilizadas;
    private int porcoesRestantes;
    private int limiteTotal;
    private LocalDateTime dataHora;
}
