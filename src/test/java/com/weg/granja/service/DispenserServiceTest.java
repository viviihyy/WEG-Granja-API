package com.weg.granja.service;

import com.weg.granja.dto.CotaResponseDTO;
import com.weg.granja.dto.DispenserResponseDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.model.Colaborador;
import com.weg.granja.model.ConfiguracaoDiaria;
import com.weg.granja.model.Retirada;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.model.Turno;
import com.weg.granja.repository.ColaboradorRepository;
import com.weg.granja.repository.RetiradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DispenserService - regras de negócio")
class DispenserServiceTest {

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private ConfiguracaoDiariaService configuracaoService;

    @Mock
    private RetiradaRepository retiradaRepository;

    @InjectMocks
    private DispenserService service;

    private Colaborador colabManha;

    @BeforeEach
    void setUp() {
        // Janela de almoço cobrindo o dia inteiro -> detectarRefeicaoAtual() sempre retorna ALMOCO,
        // tornando os testes independentes da hora real de execução.
        ReflectionTestUtils.setField(service, "almocoInicio", "00:00");
        ReflectionTestUtils.setField(service, "almocoFim", "23:59");
        ReflectionTestUtils.setField(service, "jantaInicio", "23:59");
        ReflectionTestUtils.setField(service, "jantaFim", "23:59");

        colabManha = new Colaborador(1L, "Ana Paula Ferreira", "100001", Turno.MANHA, false, true);
    }

    private ConfiguracaoDiaria configAlmoco(int limite) {
        return new ConfiguracaoDiaria(10L, LocalDate.now(), TipoRefeicao.ALMOCO, limite,
                LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("dispensar: libera porção quando colaborador do turno MANHA tira almoço dentro da cota")
    void dispensar_turnoManhaAlmoco_libera() {
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100001")).thenReturn(Optional.of(colabManha));
        when(configuracaoService.encontrarPorDataERefeicao(any(), eq(TipoRefeicao.ALMOCO)))
                .thenReturn(configAlmoco(3));
        when(retiradaRepository.contarRetiradas(any(), any(), any(), any())).thenReturn(1L);
        when(retiradaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DispenserResponseDTO result = service.dispensar("100001");

        assertThat(result.getStatus()).isEqualTo("LIBERADO");
        assertThat(result.getPorcoesUtilizadas()).isEqualTo(2);
        assertThat(result.getPorcoesRestantes()).isEqualTo(1);
        verify(retiradaRepository).save(any(Retirada.class));
    }

    @Test
    @DisplayName("dispensar: bloqueia quando a cota diária já foi atingida")
    void dispensar_cotaAtingida_bloqueia() {
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100001")).thenReturn(Optional.of(colabManha));
        when(configuracaoService.encontrarPorDataERefeicao(any(), eq(TipoRefeicao.ALMOCO)))
                .thenReturn(configAlmoco(3));
        when(retiradaRepository.contarRetiradas(any(), any(), any(), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.dispensar("100001"))
                .isInstanceOf(NegocioException.class)
                .matches(e -> ((NegocioException) e).getStatus() == HttpStatus.FORBIDDEN)
                .hasMessageContaining("Limite");

        verify(retiradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("dispensar: bloqueia colaborador do turno NOITE tentando retirar no almoço")
    void dispensar_turnoNoiteNoAlmoco_bloqueia() {
        Colaborador colabNoite = new Colaborador(2L, "Carlos", "100002", Turno.NOITE, false, true);
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100002")).thenReturn(Optional.of(colabNoite));

        assertThatThrownBy(() -> service.dispensar("100002"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("não autorizado");

        verify(retiradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("dispensar: colaborador com vale 2ª refeição é autorizado mesmo fora do turno padrão")
    void dispensar_valeSegundaRefeicao_autoriza() {
        Colaborador colabNoiteVale = new Colaborador(3L, "Diana", "100003", Turno.NOITE, true, true);
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100003")).thenReturn(Optional.of(colabNoiteVale));
        when(configuracaoService.encontrarPorDataERefeicao(any(), eq(TipoRefeicao.ALMOCO)))
                .thenReturn(configAlmoco(2));
        when(retiradaRepository.contarRetiradas(any(), any(), any(), any())).thenReturn(0L);
        when(retiradaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DispenserResponseDTO result = service.dispensar("100003");

        assertThat(result.getStatus()).isEqualTo("LIBERADO");
    }

    @Test
    @DisplayName("dispensar: lança 404 quando o crachá não é reconhecido / colaborador inativo")
    void dispensar_crachaInvalido_lanca404() {
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.dispensar("999"))
                .isInstanceOf(NegocioException.class)
                .matches(e -> ((NegocioException) e).getStatus() == HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("consultarCota: retorna autorizado=true e porções restantes corretas")
    void consultarCota_autorizado_retornaRestantes() {
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100001")).thenReturn(Optional.of(colabManha));
        when(configuracaoService.encontrarPorDataERefeicao(any(), eq(TipoRefeicao.ALMOCO)))
                .thenReturn(configAlmoco(3));
        when(retiradaRepository.contarRetiradas(any(), any(), any(), any())).thenReturn(1L);

        CotaResponseDTO result = service.consultarCota("100001", TipoRefeicao.ALMOCO);

        assertThat(result.isAutorizado()).isTrue();
        assertThat(result.getPorcoesRestantes()).isEqualTo(2);
    }

    @Test
    @DisplayName("consultarCota: sem config cadastrada retorna limites zerados sem lançar erro")
    void consultarCota_semConfig_retornaZeros() {
        when(colaboradorRepository.findByMatriculaAndAtivoTrue("100001")).thenReturn(Optional.of(colabManha));
        when(configuracaoService.encontrarPorDataERefeicao(any(), eq(TipoRefeicao.ALMOCO)))
                .thenThrow(new NegocioException("sem config", HttpStatus.NOT_FOUND));

        CotaResponseDTO result = service.consultarCota("100001", TipoRefeicao.ALMOCO);

        assertThat(result.getLimitePorcoes()).isZero();
        assertThat(result.getPorcoesRestantes()).isZero();
    }
}