package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.DashboardResumoDTO;
import com.fixhub.FixHub.model.dto.GraficoTicketsDTO;
import com.fixhub.FixHub.model.dto.DesempenhoFuncionarioDTO;
import com.fixhub.FixHub.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * Retorna um resumo geral dos tickets (cards do dashboard)
     */
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoDTO> getResumo() {
        return ResponseEntity.ok(dashboardService.getResumo());
    }

    /**
     * Retorna gráfico de tickets por status
     */
    @GetMapping("/tickets/status")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorStatus() {
        return ResponseEntity.ok(dashboardService.getTicketsPorStatus());
    }

    /**
     * Retorna gráfico de tickets por prioridade
     */
    @GetMapping("/tickets/prioridade")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorPrioridade() {
        return ResponseEntity.ok(dashboardService.getTicketsPorPrioridade());
    }

    /**
     * Retorna gráfico de tickets por equipe responsável
     */
    @GetMapping("/tickets/equipe")
    public ResponseEntity<List<GraficoTicketsDTO>> getTicketsPorEquipe() {
        return ResponseEntity.ok(dashboardService.getTicketsPorEquipe());
    }

    /**
     * Retorna ranking de desempenho dos funcionários
     */
    @GetMapping("/funcionarios/desempenho")
    public ResponseEntity<List<DesempenhoFuncionarioDTO>> getDesempenhoFuncionarios() {
        return ResponseEntity.ok(dashboardService.getDesempenhoFuncionarios());
    }
}
