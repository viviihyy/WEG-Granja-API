package com.weg.granja.dto;

import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.model.Turno;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CotaResponseDTO {
    private String colaboradorNome;
    private String matricula;
    private Turno turno;
    private boolean temSegundaRefeicao;
    private LocalDate data;
    private TipoRefeicao tipoRefeicao;
    private int limitePorcoes;
    private int porcoesUtilizadas;
    private int porcoesRestantes;
    private boolean autorizado;
}
