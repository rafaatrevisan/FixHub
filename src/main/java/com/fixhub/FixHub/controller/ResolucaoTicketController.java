package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.ResolucaoTicketRequestDTO;
import com.fixhub.FixHub.model.dto.ResolucaoTicketResponseDTO;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import com.fixhub.FixHub.model.mapper.ResolucaoTicketMapper;
import com.fixhub.FixHub.service.ResolucaoTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fixhub/resolucoes")
@RequiredArgsConstructor
public class ResolucaoTicketController {

    private final ResolucaoTicketService resolucaoService;

    @PostMapping("/assumir")
    public ResponseEntity<ResolucaoTicketResponseDTO> assumirTicket(
            @RequestParam Integer idTicketMestre) {
        ResolucaoTicket resolucao = resolucaoService.assumirTicket(idTicketMestre);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/resolver")
    public ResponseEntity<ResolucaoTicketResponseDTO> resolverTicket(
            @RequestBody ResolucaoTicketRequestDTO dto) {
        ResolucaoTicket resolucao = resolucaoService.resolverTicket(dto);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/reprovar")
    public ResponseEntity<ResolucaoTicketResponseDTO> reprovarTicket(
            @RequestParam Integer idTicketMestre) {
        ResolucaoTicket resolucao = resolucaoService.reprovarTicket(idTicketMestre);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/renunciar")
    public ResponseEntity<String> renunciarTicket(
            @RequestParam Integer idTicketMestre) {
        resolucaoService.renunciarTicket(idTicketMestre);
        return ResponseEntity.ok("Ticket Mestre e todos os tickets vinculados retornados para pendente.");
    }
}
