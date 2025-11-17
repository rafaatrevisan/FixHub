package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.DashboardResumoDTO;
import com.fixhub.FixHub.model.dto.GraficoTicketsDTO;
import com.fixhub.FixHub.model.dto.DesempenhoFuncionarioDTO;
import com.fixhub.FixHub.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * Retorna um resumo geral dos tickets (cards do dashboard)
     * Parâmetros opcionais: dataInicio e dataFim
     */
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoDTO> getResumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(dashboardService.getResumo(dataInicio, dataFim));
    }

    /**
     * Retorna gráfico de tickets por status
     */
    @GetMapping("/tickets/status")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(dashboardService.getTicketsPorStatus(dataInicio, dataFim));
    }

    /**
     * Retorna gráfico de tickets por prioridade
     */
    @GetMapping("/tickets/prioridade")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorPrioridade(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(dashboardService.getTicketsPorPrioridade(dataInicio, dataFim));
    }

    /**
     * Retorna gráfico de tickets por equipe responsável
     */
    @GetMapping("/tickets/equipe")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorEquipe(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(dashboardService.getTicketsPorEquipe(dataInicio, dataFim));
    }

    /**
     * Retorna ranking de desempenho dos funcionários
     */
    @GetMapping("/funcionarios/desempenho")
    public ResponseEntity<List<DesempenhoFuncionarioDTO>> getDesempenhoFuncionarios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(dashboardService.getDesempenhoFuncionarios(dataInicio, dataFim));
    }
}
