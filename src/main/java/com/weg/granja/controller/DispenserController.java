package com.weg.granja.controller;

import com.weg.granja.dto.CotaResponseDTO;
import com.weg.granja.dto.DispenserResponseDTO;
import com.weg.granja.model.TipoRefeicao;
import com.weg.granja.service.DispenserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dispenser")
@RequiredArgsConstructor
public class DispenserController {

    private final DispenserService service;

    /**
     * Simula a leitura do crachá na máquina dispensadora.
     * Detecta automaticamente se é almoço ou janta pelo horário atual.
     * Libera 1 porção se o colaborador estiver autorizado e com cota disponível.
     *
     * POST /dispenser/dispensar/{matricula}
     */
    @PostMapping("/dispensar/{matricula}")
    public ResponseEntity<DispenserResponseDTO> dispensar(@PathVariable String matricula) {
        return ResponseEntity.ok(service.dispensar(matricula));
    }

    /**
     * Consulta a cota do colaborador sem realizar retirada.
     * Útil para exibir no display da máquina antes de confirmar.
     *
     * GET /dispenser/cota/{matricula}?tipoRefeicao=ALMOCO
     */
    @GetMapping("/cota/{matricula}")
    public ResponseEntity<CotaResponseDTO> consultarCota(
            @PathVariable String matricula,
            @RequestParam(required = false) TipoRefeicao tipoRefeicao) {
        return ResponseEntity.ok(service.consultarCota(matricula, tipoRefeicao));
    }
}
