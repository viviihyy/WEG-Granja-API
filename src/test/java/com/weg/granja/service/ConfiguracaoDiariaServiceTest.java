package com.weg.granja.service;

import com.weg.granja.dto.ConfiguracaoDiariaDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.mapper.Mapper;
import com.weg.granja.model.ConfiguracaoDiaria;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.repository.ConfiguracaoDiariaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracaoDiariaService - testes unitários")
class ConfiguracaoDiariaServiceTest {

    @Mock
    private ConfiguracaoDiariaRepository repository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private ConfiguracaoDiariaService service;

    private final LocalDate hoje = LocalDate.of(2025, 6, 10);

    private ConfiguracaoDiaria config;
    private ConfiguracaoDiariaDTO.Request request;
    private ConfiguracaoDiariaDTO.Response response;

    @BeforeEach
    void setUp() {
        config = new ConfiguracaoDiaria(1L, hoje, TipoRefeicao.ALMOCO, 150,
                LocalDateTime.now(), null);
        request = new ConfiguracaoDiariaDTO.Request(hoje, TipoRefeicao.ALMOCO, 150);
        response = new ConfiguracaoDiariaDTO.Response(1L, hoje, TipoRefeicao.ALMOCO, 150,
                LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("salvar: cria nova configuração quando não existe para a data/refeição")
    void salvar_novaConfig_criada() {
        when(repository.findByDataAndTipoRefeicao(hoje, TipoRefeicao.ALMOCO)).thenReturn(Optional.empty());
        when(mapper.toConfiguracao(request)).thenReturn(config);
        when(repository.save(any())).thenReturn(config);
        when(mapper.toConfiguracaoResponse(config)).thenReturn(response);

        ConfiguracaoDiariaDTO.Response result = service.salvar(request);

        assertThat(result.getLimitePorcoes()).isEqualTo(150);
    }

    @Test
    @DisplayName("salvar: faz upsert atualizando o limite quando já existe config para a data/refeição")
    void salvar_configExistente_atualizaLimite() {
        config.setLimitePorcoes(100);
        when(repository.findByDataAndTipoRefeicao(hoje, TipoRefeicao.ALMOCO)).thenReturn(Optional.of(config));
        when(repository.save(any())).thenReturn(config);
        when(mapper.toConfiguracaoResponse(config)).thenReturn(response);

        service.salvar(request);

        ArgumentCaptor<ConfiguracaoDiaria> captor = ArgumentCaptor.forClass(ConfiguracaoDiaria.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLimitePorcoes()).isEqualTo(150);
    }

    @Test
    @DisplayName("atualizar: bloqueia quando a data/refeição conflita com outro registro")
    void atualizar_conflito_lancaExcecao() {
        ConfiguracaoDiaria outro = new ConfiguracaoDiaria(2L, hoje, TipoRefeicao.ALMOCO, 200,
                LocalDateTime.now(), null);
        when(repository.findById(1L)).thenReturn(Optional.of(config));
        when(repository.findByDataAndTipoRefeicao(hoje, TipoRefeicao.ALMOCO)).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Já existe configuração");
    }

    @Test
    @DisplayName("encontrarPorDataERefeicao: lança 404 quando não há config cadastrada")
    void encontrarPorDataERefeicao_inexistente_lanca404() {
        when(repository.findByDataAndTipoRefeicao(hoje, TipoRefeicao.JANTA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encontrarPorDataERefeicao(hoje, TipoRefeicao.JANTA))
                .isInstanceOf(NegocioException.class)
                .matches(e -> ((NegocioException) e).getStatus() == HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deletar: remove a configuração existente")
    void deletar_existente_remove() {
        when(repository.findById(1L)).thenReturn(Optional.of(config));

        service.deletar(1L);

        org.mockito.Mockito.verify(repository).delete(config);
    }
}