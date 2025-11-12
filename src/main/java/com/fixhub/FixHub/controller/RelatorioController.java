package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.RelatorioTicketsDTO;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/relatorios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RelatorioController {

    private final RelatorioService relatorioService;

    /**
     * Endpoint para buscar relatório de tickets (já existente)
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<RelatorioTicketsDTO>> getRelatorioTickets(
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

    /**
     * Endpoint para exportar relatório em CSV
     */
    @GetMapping("/tickets/exportar/csv")
    public ResponseEntity<byte[]> exportarRelatorioCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusTicket status,
            @RequestParam(required = false) PrioridadeTicket prioridade,
            @RequestParam(required = false) String equipe,
            @RequestParam(required = false) String funcionario
    ) {
        byte[] csvData = relatorioService.exportarRelatorioCSV(
                dataInicio, dataFim, status, prioridade, equipe, funcionario
        );

        // Nome do arquivo com timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "relatorio_tickets_" + timestamp + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}
