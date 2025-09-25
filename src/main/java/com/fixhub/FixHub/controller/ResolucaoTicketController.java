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
            @RequestParam Integer idTicket,
            @RequestParam Integer idFuncionario) {
        ResolucaoTicket resolucao = resolucaoService.assumirTicket(idTicket, idFuncionario);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/resolver")
    public ResponseEntity<ResolucaoTicketResponseDTO> resolverTicket(@RequestBody ResolucaoTicketRequestDTO dto) {
        ResolucaoTicket resolucao = resolucaoService.resolverTicket(dto);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/reprovar")
    public ResponseEntity<ResolucaoTicketResponseDTO> reprovarTicket(
            @RequestParam Integer idTicket,
            @RequestParam Integer idFuncionario) {
        ResolucaoTicket resolucao = resolucaoService.reprovarTicket(idTicket, idFuncionario);
        return ResponseEntity.ok(ResolucaoTicketMapper.toDTO(resolucao));
    }

    @PostMapping("/renunciar")
    public ResponseEntity<String> largarTicket(
            @RequestParam Integer idTicket,
            @RequestParam Integer idFuncionario) {
        resolucaoService.renunciarTicket(idTicket, idFuncionario);
        return ResponseEntity.ok("Ticket retornado para pendente e vínculo removido.");
    }
}
