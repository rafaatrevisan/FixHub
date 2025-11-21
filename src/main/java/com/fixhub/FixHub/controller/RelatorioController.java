package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.RelatorioTicketsDTO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/admin/relatorios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RelatorioController {

    private final RelatorioService relatorioService;

    /**
     * Endpoint para buscar relatório de tickets com FILTROS MÚLTIPLOS
     * Parâmetros podem ser enviados como: ?status=PENDENTE,EM_ANDAMENTO&prioridade=ALTA,URGENTE
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<RelatorioTicketsDTO>> getRelatorioTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) String equipe,
            @RequestParam(required = false) String funcionario
    ) {
        List<String> statusList = converterParaLista(status);
        List<String> prioridadeList = converterParaLista(prioridade);
        List<String> equipeList = converterParaLista(equipe);

        List<RelatorioTicketsDTO> relatorio = relatorioService.gerarRelatorioTickets(
                dataInicio, dataFim, statusList, prioridadeList, equipeList, funcionario
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) String equipe,
            @RequestParam(required = false) String funcionario
    ) {
        List<String> statusList = converterParaLista(status);
        List<String> prioridadeList = converterParaLista(prioridade);
        List<String> equipeList = converterParaLista(equipe);

        byte[] csvData = relatorioService.exportarRelatorioCSV(
                dataInicio, dataFim, statusList, prioridadeList, equipeList, funcionario
        );

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "relatorio_tickets_" + timestamp + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    /**
     * Converte string separada por vírgulas em lista
     * Exemplo: "PENDENTE,EM_ANDAMENTO" → ["PENDENTE", "EM_ANDAMENTO"]
     */
    private List<String> converterParaLista(String valor) {
        if (valor == null || valor.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
