package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.ResolucaoTicketRequestDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.model.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ResolucaoTicketService {

    private final TicketRepository ticketRepository;
    private final PessoaRepository pessoaRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;

    public ResolucaoTicket resolverTicket(ResolucaoTicketRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser resolvidos");
        }

        Pessoa funcionario = pessoaRepository.findById(dto.getIdFuncionario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        ticket.setStatus(StatusTicket.CONCLUIDO);
        ticketRepository.save(ticket);

        ResolucaoTicket resolucao = ResolucaoTicket.builder()
                .ticket(ticket)
                .funcionario(funcionario)
                .descricao(dto.getDescricao())
                .build();

        return resolucaoTicketRepository.save(resolucao);
    }

    public ResolucaoTicket reprovarTicket(Integer idTicket, Integer idFuncionario) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser reprovados");
        }

        Pessoa funcionario = pessoaRepository.findById(idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        ticket.setStatus(StatusTicket.REPROVADO);
        ticketRepository.save(ticket);

        ResolucaoTicket resolucao = ResolucaoTicket.builder()
                .ticket(ticket)
                .funcionario(funcionario)
                .descricao("Ticket fake/incoerente")
                .build();

        return resolucaoTicketRepository.save(resolucao);
    }
}
