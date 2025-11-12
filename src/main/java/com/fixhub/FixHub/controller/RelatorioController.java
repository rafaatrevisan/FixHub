package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.RelatorioTicketsDTO;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    /**
     * Gera relatório detalhado dos tickets com filtros
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<RelatorioTicketsDTO>> gerarRelatorioTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusTicket status,
            @RequestParam(required = false) PrioridadeTicket prioridade,
            @RequestParam(required = false) String equipe,
            @RequestParam(required = false) String funcionario
    ) {
        List<RelatorioTicketsDTO> relatorio = relatorioService.gerarRelatorioTickets(
                dataInicio, dataFim, status, prioridade, equipe, funcionario
        );
        return ResponseEntity.ok(relatorio);
    }
}
