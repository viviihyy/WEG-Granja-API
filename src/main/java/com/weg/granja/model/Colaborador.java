package com.weg.granja.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "colaboradores")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Turno turno;

    /*
     * Vale segunda refeição: quando true, o colaborador pode retirar
     * proteína tanto no ALMOCO quanto na JANTA, independente do turno.
     */
    @Column(nullable = false)
    private boolean temSegundaRefeicao = false;

    @Column(nullable = false)
    private boolean ativo = true;
}
