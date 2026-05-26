package com.weg.granja.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "configuracoes_diarias",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_data_refeicao",
        columnNames = {"data", "tipo_refeicao"}
    )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_refeicao", nullable = false, length = 10)
    private TipoRefeicao tipoRefeicao;

    /*
     * Quantidade máxima de pedaços de proteína por colaborador
     * nesta data e refeição.
     */
    @Column(nullable = false)
    private int limitePorcoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
