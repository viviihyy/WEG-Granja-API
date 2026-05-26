package com.weg.granja.service;

import com.weg.granja.dto.ColaboradorDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.mapper.Mapper;
import com.weg.granja.model.Colaborador;
import com.weg.granja.repository.ColaboradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final Mapper mapper;

    @Transactional(readOnly = true)
    public List<ColaboradorDTO.Response> listarAtivos() {
        return repository.findAllByAtivoTrue()
            .stream()
            .map(mapper::toColaboradorResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ColaboradorDTO.Response> listarTodos() {
        return repository.findAll()
            .stream()
            .map(mapper::toColaboradorResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorDTO.Response buscarPorId(Long id) {
        return mapper.toColaboradorResponse(encontrarPorId(id));
    }

    @Transactional(readOnly = true)
    public ColaboradorDTO.Response buscarPorMatricula(String matricula) {
        Colaborador c = repository.findByMatricula(matricula)
            .orElseThrow(() -> new NegocioException(
                "Colaborador com matrícula '" + matricula + "' não encontrado.",
                HttpStatus.NOT_FOUND
            ));
        return mapper.toColaboradorResponse(c);
    }

    @Transactional
    public ColaboradorDTO.Response cadastrar(ColaboradorDTO.Request dto) {
        if (repository.existsByMatricula(dto.getMatricula())) {
            throw new NegocioException("Já existe um colaborador com a matrícula '" + dto.getMatricula() + "'.");
        }
        Colaborador salvo = repository.save(mapper.toColaborador(dto));
        return mapper.toColaboradorResponse(salvo);
    }

    @Transactional
    public ColaboradorDTO.Response atualizar(Long id, ColaboradorDTO.Request dto) {
        Colaborador existente = encontrarPorId(id);

        // Valida matrícula duplicada (ignora o próprio registro)
        repository.findByMatricula(dto.getMatricula()).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new NegocioException("Matrícula '" + dto.getMatricula() + "' já está em uso por outro colaborador.");
            }
        });

        existente.setNome(dto.getNome());
        existente.setMatricula(dto.getMatricula());
        existente.setTurno(dto.getTurno());
        existente.setTemSegundaRefeicao(dto.isTemSegundaRefeicao());

        return mapper.toColaboradorResponse(repository.save(existente));
    }

    @Transactional
    public void inativar(Long id) {
        Colaborador c = encontrarPorId(id);
        c.setAtivo(false);
        repository.save(c);
    }

    @Transactional
    public void reativar(Long id) {
        Colaborador c = encontrarPorId(id);
        c.setAtivo(true);
        repository.save(c);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Colaborador encontrarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NegocioException(
                "Colaborador com id " + id + " não encontrado.",
                HttpStatus.NOT_FOUND
            ));
    }
}
