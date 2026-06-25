package com.weg.granja.service;

import com.weg.granja.dto.CotaResponseDTO;
import com.weg.granja.dto.DispenserResponseDTO;
import com.weg.granja.exception.NegocioException;
import com.weg.granja.model.*;
import com.weg.granja.repository.ColaboradorRepository;
import com.weg.granja.repository.RetiradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DispenserService {

    private final ColaboradorRepository colaboradorRepository;
    private final ConfiguracaoDiariaService configuracaoService;
    private final RetiradaRepository retiradaRepository;

    @Value("${app.refeicao.almoco.inicio:10:30}")
    private String almocoInicio;

    @Value("${app.refeicao.almoco.fim:15:00}")
    private String almocoFim;

    @Value("${app.refeicao.janta.inicio:16:00}")
    private String jantaInicio;

    @Value("${app.refeicao.janta.fim:22:00}")
    private String jantaFim;

    @Value("${app.timezone:America/Sao_Paulo}")
    private String timezone;

    // ── Dispensagem (leitura de crachá) ─────────────────────────────────────

    @Transactional
    public DispenserResponseDTO dispensar(String matricula) {
        Colaborador colaborador = buscarColaboradorAtivo(matricula);
        TipoRefeicao refeicaoAtual = detectarRefeicaoAtual();

        verificarAutorizacaoTurno(colaborador, refeicaoAtual);

        LocalDate hoje = LocalDate.now();
        ConfiguracaoDiaria config = configuracaoService.encontrarPorDataERefeicao(hoje, refeicaoAtual);

        long jaRetiradas = contarRetiradasHoje(colaborador, refeicaoAtual, hoje);

        if (jaRetiradas >= config.getLimitePorcoes()) {
            throw new NegocioException(
                    "Limite de " + config.getLimitePorcoes() + " porção(ões) atingido. Retirada bloqueada.",
                    HttpStatus.FORBIDDEN
            );
        }

        // Registra a retirada — sempre 1 porção por leitura (RN03)
        Retirada retirada = new Retirada();
        retirada.setColaborador(colaborador);
        retirada.setTipoRefeicao(refeicaoAtual);
        retirada.setQuantidade(1);
        retiradaRepository.save(retirada);

        long utilizadas = jaRetiradas + 1;
        long restantes = config.getLimitePorcoes() - utilizadas;

        return new DispenserResponseDTO(
                "LIBERADO",
                "✅ Porção liberada! Bom apetite, " + colaborador.getNome().split(" ")[0] + "!",
                colaborador.getNome(),
                colaborador.getMatricula(),
                refeicaoAtual,
                (int) utilizadas,
                (int) restantes,
                config.getLimitePorcoes(),
                LocalDateTime.now()
        );
    }

    // ── Consulta de cota (sem dispensar) ────────────────────────────────────

    @Transactional(readOnly = true)
    public CotaResponseDTO consultarCota(String matricula, TipoRefeicao tipoRefeicao) {
        Colaborador colaborador = buscarColaboradorAtivo(matricula);
        LocalDate hoje = LocalDate.now();

        // Se tipoRefeicao não foi informado, detecta pelo horário atual
        TipoRefeicao refeicao = tipoRefeicao != null ? tipoRefeicao : detectarRefeicaoAtual();

        boolean autorizado = colaboradorAutorizadoParaRefeicao(colaborador, refeicao);

        int limite = 0;
        long utilizadas = 0;

        try {
            ConfiguracaoDiaria config = configuracaoService.encontrarPorDataERefeicao(hoje, refeicao);
            limite = config.getLimitePorcoes();
            utilizadas = contarRetiradasHoje(colaborador, refeicao, hoje);
        } catch (NegocioException e) {
            // Configuração ainda não cadastrada — retorna zeros
        }

        return new CotaResponseDTO(
                colaborador.getNome(),
                colaborador.getMatricula(),
                colaborador.getTurno(),
                colaborador.isTemSegundaRefeicao(),
                hoje,
                refeicao,
                limite,
                (int) utilizadas,
                (int) Math.max(0, limite - utilizadas),
                autorizado
        );
    }

    // ── Regras de turno ─────────────────────────────────────────────────────

    /**
     * RN02 — Verifica se o colaborador pode retirar na refeição atual.
     *
     * - Vale segunda refeição → acesso a ALMOÇO e JANTA.
     * - MANHÃ e TARDE → apenas ALMOÇO.
     * - NOITE → apenas JANTA.
     */
    private void verificarAutorizacaoTurno(Colaborador colaborador, TipoRefeicao refeicao) {
        if (!colaboradorAutorizadoParaRefeicao(colaborador, refeicao)) {
            String turnoLabel = colaborador.getTurno().name();
            String refeicaoLabel = refeicao == TipoRefeicao.ALMOCO ? "almoço" : "janta";
            throw new NegocioException(
                    "Colaborador do turno " + turnoLabel + " não autorizado para retirada no " + refeicaoLabel +
                            ". Apenas colaboradores com vale segunda refeição podem retirar em ambos os turnos.",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private boolean colaboradorAutorizadoParaRefeicao(Colaborador colaborador, TipoRefeicao refeicao) {
        if (colaborador.isTemSegundaRefeicao()) {
            return true; // Acesso irrestrito às duas refeições
        }
        return switch (refeicao) {
            case ALMOCO -> colaborador.getTurno() == Turno.MANHA || colaborador.getTurno() == Turno.TARDE;
            case JANTA  -> colaborador.getTurno() == Turno.NOITE;
        };
    }

    // ── Detecção de refeição pelo horário ───────────────────────────────────

    private TipoRefeicao detectarRefeicaoAtual() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime agora = LocalTime.now(ZoneId.of(timezone));
        LocalTime almocoStart = LocalTime.parse(almocoInicio, fmt);
        LocalTime almocoEnd   = LocalTime.parse(almocoFim, fmt);
        LocalTime jantaStart  = LocalTime.parse(jantaInicio, fmt);
        LocalTime jantaEnd    = LocalTime.parse(jantaFim, fmt);

        if (!agora.isBefore(almocoStart) && agora.isBefore(almocoEnd)) {
            return TipoRefeicao.ALMOCO;
        }
        if (!agora.isBefore(jantaStart) && agora.isBefore(jantaEnd)) {
            return TipoRefeicao.JANTA;
        }
        throw new NegocioException(
                "Refeitório fechado no momento. Horário do almoço: " + almocoInicio + " às " + almocoFim +
                        " | Horário da janta: " + jantaInicio + " às " + jantaFim + ".",
                HttpStatus.FORBIDDEN
        );
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Colaborador buscarColaboradorAtivo(String matricula) {
        return colaboradorRepository.findByMatriculaAndAtivoTrue(matricula)
                .orElseThrow(() -> new NegocioException(
                        "Crachá não reconhecido ou colaborador inativo. Matrícula: " + matricula,
                        HttpStatus.NOT_FOUND
                ));
    }

    private long contarRetiradasHoje(Colaborador colaborador, TipoRefeicao refeicao, LocalDate data) {
        return retiradaRepository.contarRetiradas(
                colaborador,
                refeicao,
                data.atStartOfDay(),
                data.atTime(23, 59, 59)
        );
    }
}