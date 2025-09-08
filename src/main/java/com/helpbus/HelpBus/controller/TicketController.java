package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.model.dto.TicketDTO;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.entity.Ticket;
import com.helpbus.HelpBus.model.mapper.TicketMapper;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import com.helpbus.HelpBus.model.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final PessoaRepository pessoaRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TicketDTO> listarTodos() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketDTO> criar(@RequestBody TicketDTO dto) {
        Pessoa usuario = pessoaRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Ticket ticket = TicketMapper.toEntity(dto, usuario);
        Ticket salvo = ticketRepository.save(ticket);

        return ResponseEntity.ok(TicketMapper.toDTO(salvo));
    }

    @GetMapping("{id}")
    public TicketDTO buscarPorId(@PathVariable Integer id) {
        return ticketRepository.findById(id)
                .map(TicketMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody TicketDTO dto) {
        ticketRepository.findById(id)
                .map(ticket -> {
                    Pessoa usuario = pessoaRepository.findById(dto.getIdUsuario())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

                    Ticket atualizado = TicketMapper.toEntity(dto, usuario);
                    atualizado.setId(ticket.getId());
                    ticketRepository.save(atualizado);
                    return atualizado;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        ticketRepository.findById(id)
                .map(ticket -> {
                    ticketRepository.delete(ticket);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
    }
}
