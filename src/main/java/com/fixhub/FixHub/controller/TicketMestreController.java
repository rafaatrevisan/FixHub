package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.service.TicketMestreService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/tickets-mestre")
@RequiredArgsConstructor
public class TicketMestreController {

    private final TicketMestreService ticketMestreService;

    @GetMapping("/filtro")
    public ResponseEntity<List<TicketMestre>> listarComFiltros(
            @RequestParam Integer idFuncionario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusTicket status,
            @RequestParam(required = false) PrioridadeTicket prioridade,
            @RequestParam(required = false) String andar
    ) {
        List<TicketMestre> lista = ticketMestreService.listarTicketsMestreComFiltros(
                dataInicio, dataFim, status, prioridade, andar
        );
        return ResponseEntity.ok(lista);
    }
}
