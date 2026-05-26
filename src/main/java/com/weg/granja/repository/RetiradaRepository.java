package com.weg.granja.repository;

import com.weg.granja.model.Colaborador;
import com.weg.granja.model.Retirada;
import com.weg.granja.model.TipoRefeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RetiradaRepository extends JpaRepository<Retirada, Long> {

    // Contagem de retiradas de um colaborador em uma data/refeição — usada para controle de cota
    @Query("""
        SELECT COUNT(r) FROM Retirada r
        WHERE r.colaborador = :colaborador
          AND r.tipoRefeicao = :tipoRefeicao
          AND r.dataHora >= :inicio
          AND r.dataHora <= :fim
    """)
    long contarRetiradas(
        @Param("colaborador") Colaborador colaborador,
        @Param("tipoRefeicao") TipoRefeicao tipoRefeicao,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Histórico geral com filtro opcional por data
    @Query("""
        SELECT r FROM Retirada r
        JOIN FETCH r.colaborador
        WHERE r.dataHora >= :inicio AND r.dataHora <= :fim
        ORDER BY r.dataHora DESC
    """)
    List<Retirada> findHistorico(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // Histórico de um colaborador específico
    @Query("""
        SELECT r FROM Retirada r
        JOIN FETCH r.colaborador
        WHERE r.colaborador.id = :colaboradorId
        ORDER BY r.dataHora DESC
    """)
    List<Retirada> findByColaboradorId(@Param("colaboradorId") Long colaboradorId);

    // Todas as retiradas do dia agrupáveis para relatório
    @Query("""
        SELECT r FROM Retirada r
        JOIN FETCH r.colaborador
        WHERE r.dataHora >= :inicio AND r.dataHora <= :fim
          AND (:tipoRefeicao IS NULL OR r.tipoRefeicao = :tipoRefeicao)
        ORDER BY r.dataHora DESC
    """)
    List<Retirada> findByDataERefeicao(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("tipoRefeicao") TipoRefeicao tipoRefeicao
    );
}
