package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.TicketDetalhesDTO;
import com.fixhub.FixHub.model.dto.TicketRequestDTO;
import com.fixhub.FixHub.model.dto.TicketResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.mapper.TicketMapper;
import com.fixhub.FixHub.service.TicketService;
import com.fixhub.FixHub.util.AuthUtil;
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
    private final AuthUtil authUtil;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TicketResponseDTO> listarTodos() {
        return ticketService.getAllTickets()
                .stream()
                .map(TicketMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketResponseDTO> criar(@RequestBody TicketRequestDTO dto) {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        Ticket ticket = TicketMapper.toEntity(dto, usuario);
        Ticket salvo = ticketService.criarTicket(ticket);
        return ResponseEntity.ok(TicketMapper.toResponseDTO(salvo));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody TicketRequestDTO dto) {
        try {
            Pessoa usuario = authUtil.getPessoaUsuarioLogado();
            Ticket ticket = TicketMapper.toEntity(dto, usuario);
            Ticket atualizado = ticketService.atualizarTicket(id, ticket);
            return ResponseEntity.ok(TicketMapper.toResponseDTO(atualizado));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("{id}")
    public TicketResponseDTO buscarPorId(@PathVariable Integer id) {
        return TicketMapper.toResponseDTO(ticketService.getTicketById(id));
    }

    @GetMapping("{id}/detalhes")
    public TicketDetalhesDTO buscarDetalhes(@PathVariable Integer id) {
        return ticketService.buscarTicketComResolucao(id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
    }

    @PostMapping("/lixeira/{idLixeira}")
    public ResponseEntity<String> criarTicketPorLixeira(@PathVariable Integer idLixeira) {
        ticketService.criarTicketPorLixeira(idLixeira);
        return ResponseEntity.ok("Ticket criado");
    }

}
