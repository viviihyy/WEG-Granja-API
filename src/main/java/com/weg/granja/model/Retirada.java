package com.weg.granja.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "retiradas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Retirada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_refeicao", nullable = false, length = 10)
    private TipoRefeicao tipoRefeicao;

    /*
     * Sempre 1 — a dispensadora libera uma porção por leitura de crachá (RN03).
     */
    @Column(nullable = false)
    private int quantidade = 1;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
        this.quantidade = 1;
    }
}
