package com.weg.granja.repository;

import com.weg.granja.model.ConfiguracaoDiaria;
import com.weg.granja.model.TipoRefeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracaoDiariaRepository extends JpaRepository<ConfiguracaoDiaria, Long> {

    Optional<ConfiguracaoDiaria> findByDataAndTipoRefeicao(LocalDate data, TipoRefeicao tipoRefeicao);

    List<ConfiguracaoDiaria> findByDataOrderByTipoRefeicao(LocalDate data);

    List<ConfiguracaoDiaria> findAllByOrderByDataDescTipoRefeicaoAsc();

    boolean existsByDataAndTipoRefeicao(LocalDate data, TipoRefeicao tipoRefeicao);
}
