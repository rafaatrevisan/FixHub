package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.model.dto.TicketDTO;
import com.helpbus.HelpBus.model.entity.Ticket;
import com.helpbus.HelpBus.model.mapper.TicketMapper;
import com.helpbus.HelpBus.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TicketDTO> listarTodos() {
        return ticketService.getAllTickets()
                .stream()
                .map(TicketMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketDTO> criar(@RequestBody TicketDTO dto) {
        Ticket ticket = TicketMapper.toEntity(dto, null);
        Ticket salvo = ticketService.criarTicket(ticket, dto.getIdUsuario());
        return ResponseEntity.ok(TicketMapper.toDTO(salvo));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody TicketDTO dto) {
        try {
            Ticket ticket = TicketMapper.toEntity(dto, null);
            ticketService.atualizarTicket(id, ticket, dto.getIdUsuario());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("{id}")
    public TicketDTO buscarPorId(@PathVariable Integer id) {
        return TicketMapper.toDTO(ticketService.getTicketById(id));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
    }
}
