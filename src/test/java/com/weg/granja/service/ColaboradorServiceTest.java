package com.weg.granja.service;

import com.weg.granja.dto.ColaboradorDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.mapper.Mapper;
import com.weg.granja.model.Colaborador;
import com.weg.granja.model.Turno;
import com.weg.granja.repository.ColaboradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ColaboradorService - testes unitários")
class ColaboradorServiceTest {

    @Mock
    private ColaboradorRepository repository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private ColaboradorService service;

    private Colaborador colaborador;
    private ColaboradorDTO.Request request;
    private ColaboradorDTO.Response response;

    @BeforeEach
    void setUp() {
        colaborador = new Colaborador(1L, "Ana Paula", "100001", Turno.MANHA, false, true);
        request = new ColaboradorDTO.Request("Ana Paula", "100001", Turno.MANHA, false);
        response = new ColaboradorDTO.Response(1L, "Ana Paula", "100001", Turno.MANHA, false, true);
    }

    @Test
    @DisplayName("cadastrar: deve salvar quando a matrícula é inédita")
    void cadastrar_matriculaInedita_salva() {
        when(repository.existsByMatricula("100001")).thenReturn(false);
        when(mapper.toColaborador(request)).thenReturn(colaborador);
        when(repository.save(colaborador)).thenReturn(colaborador);
        when(mapper.toColaboradorResponse(colaborador)).thenReturn(response);

        ColaboradorDTO.Response result = service.cadastrar(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatricula()).isEqualTo("100001");
        verify(repository).save(colaborador);
    }

    @Test
    @DisplayName("cadastrar: deve lançar NegocioException quando a matrícula já existe")
    void cadastrar_matriculaDuplicada_lancaExcecao() {
        when(repository.existsByMatricula("100001")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("100001");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId: deve lançar NegocioException 404 quando não encontra")
    void buscarPorId_inexistente_lanca404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(NegocioException.class)
                .matches(e -> ((NegocioException) e).getStatus() == HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("buscarPorMatricula: deve retornar o colaborador quando existe")
    void buscarPorMatricula_existente_retorna() {
        when(repository.findByMatricula("100001")).thenReturn(Optional.of(colaborador));
        when(mapper.toColaboradorResponse(colaborador)).thenReturn(response);

        ColaboradorDTO.Response result = service.buscarPorMatricula("100001");

        assertThat(result.getNome()).isEqualTo("Ana Paula");
    }

    @Test
    @DisplayName("atualizar: deve bloquear quando a matrícula pertence a outro colaborador")
    void atualizar_matriculaDeOutro_lancaExcecao() {
        Colaborador outro = new Colaborador(2L, "Outro", "100001", Turno.NOITE, false, true);
        when(repository.findById(1L)).thenReturn(Optional.of(colaborador));
        when(repository.findByMatricula("100001")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("já está em uso");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("inativar: deve marcar ativo=false (soft delete)")
    void inativar_marcaInativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(colaborador));
        when(repository.save(any())).thenReturn(colaborador);

        service.inativar(1L);

        assertThat(colaborador.isAtivo()).isFalse();
        verify(repository).save(colaborador);
    }

    @Test
    @DisplayName("reativar: deve marcar ativo=true")
    void reativar_marcaAtivo() {
        colaborador.setAtivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(colaborador));
        when(repository.save(any())).thenReturn(colaborador);

        service.reativar(1L);

        assertThat(colaborador.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("listarAtivos: deve mapear apenas os ativos retornados pelo repositório")
    void listarAtivos_retornaLista() {
        when(repository.findAllByAtivoTrue()).thenReturn(List.of(colaborador));
        when(mapper.toColaboradorResponse(colaborador)).thenReturn(response);

        List<ColaboradorDTO.Response> result = service.listarAtivos();

        assertThat(result).hasSize(1);
        verify(repository).findAllByAtivoTrue();
    }
}