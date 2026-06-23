package com.weg.granja.repository;

import com.weg.granja.model.Colaborador;
import com.weg.granja.model.Retirada;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.model.Turno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RetiradaRepository - testes de persistência (H2)")
class RetiradaRepositoryTest {

    @Autowired
    private RetiradaRepository retiradaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Test
    @DisplayName("contarRetiradas: conta apenas as retiradas do colaborador na refeição e janela do dia")
    void contarRetiradas_filtraCorretamente() {
        Colaborador colab = colaboradorRepository.save(
                new Colaborador(null, "Ana", "100001", Turno.MANHA, false, true));

        Retirada r1 = new Retirada();
        r1.setColaborador(colab);
        r1.setTipoRefeicao(TipoRefeicao.ALMOCO);
        retiradaRepository.save(r1); // @PrePersist define dataHora = now() e quantidade = 1

        Retirada r2 = new Retirada();
        r2.setColaborador(colab);
        r2.setTipoRefeicao(TipoRefeicao.ALMOCO);
        retiradaRepository.save(r2);

        // Uma retirada de JANTA não deve entrar na contagem de ALMOCO
        Retirada r3 = new Retirada();
        r3.setColaborador(colab);
        r3.setTipoRefeicao(TipoRefeicao.JANTA);
        retiradaRepository.save(r3);

        LocalDate hoje = LocalDate.now();
        long total = retiradaRepository.contarRetiradas(
                colab, TipoRefeicao.ALMOCO, hoje.atStartOfDay(), hoje.atTime(23, 59, 59));

        assertThat(total).isEqualTo(2);
    }

    @Test
    @DisplayName("findByColaboradorId: retorna o histórico do colaborador")
    void findByColaboradorId_retornaHistorico() {
        Colaborador colab = colaboradorRepository.save(
                new Colaborador(null, "Bruno", "100002", Turno.NOITE, false, true));

        Retirada r = new Retirada();
        r.setColaborador(colab);
        r.setTipoRefeicao(TipoRefeicao.JANTA);
        retiradaRepository.save(r);

        assertThat(retiradaRepository.findByColaboradorId(colab.getId())).hasSize(1);
    }
}