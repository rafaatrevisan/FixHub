package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PessoaRepository pessoaRepository;
    private final GeminiService geminiService;

    public Ticket criarTicket(Ticket ticket, Integer idUsuario) {
        Pessoa usuario = pessoaRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        ticket.setUsuario(usuario);
        ticket.setStatus(StatusTicket.PENDENTE);

        GeminiService.GeminiResult resultadoIA = geminiService.avaliarTicket(
                ticket.getDescricaoTicketUsuario(),
                ticket.getLocalizacao(),
                ticket.getDescricaoLocalizacao(),
                ticket.getAndar()
        );

        ticket.setPrioridade(resultadoIA.prioridade());
        ticket.setEquipeResponsavel(resultadoIA.equipeResponsavel());

        return ticketRepository.save(ticket);
    }

    public Ticket atualizarTicket(Integer id, Ticket ticketAtualizado, Integer idUsuario) {
        return ticketRepository.findById(id)
                .map(ticketExistente -> {
                    if (ticketExistente.getStatus() != StatusTicket.PENDENTE) {
                        throw new IllegalStateException("O ticket deve estar pendente para ser atualizado");
                    }

                    Pessoa usuario = pessoaRepository.findById(idUsuario)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

                    ticketExistente.setUsuario(usuario);
                    ticketExistente.setAndar(ticketAtualizado.getAndar());
                    ticketExistente.setLocalizacao(ticketAtualizado.getLocalizacao());
                    ticketExistente.setDescricaoLocalizacao(ticketAtualizado.getDescricaoLocalizacao());
                    ticketExistente.setDescricaoTicketUsuario(ticketAtualizado.getDescricaoTicketUsuario());
                    ticketExistente.setImagem(ticketAtualizado.getImagem());

                    GeminiService.GeminiResult resultadoIA = geminiService.avaliarTicket(
                            ticketExistente.getDescricaoTicketUsuario(),
                            ticketExistente.getLocalizacao(),
                            ticketExistente.getDescricaoLocalizacao(),
                            ticketExistente.getAndar()
                    );

                    ticketExistente.setPrioridade(resultadoIA.prioridade());
                    ticketExistente.setEquipeResponsavel(resultadoIA.equipeResponsavel());

                    return ticketRepository.save(ticketExistente);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
    }

    public void deleteTicket(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser excluídos");
        }
        ticketRepository.save(ticket);
    }
}
