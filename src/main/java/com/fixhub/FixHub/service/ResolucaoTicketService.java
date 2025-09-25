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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResolucaoTicketService {

    private final TicketRepository ticketRepository;
    private final PessoaRepository pessoaRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;

    /**
     * Método para assumir um ticket e iniciar o trabalho.
     */
    public ResolucaoTicket assumirTicket(Integer idTicket, Integer idFuncionario) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser assumidos.");
        }

        Pessoa funcionario = pessoaRepository.findById(idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        ticket.setStatus(StatusTicket.EM_ANDAMENTO);
        ticket.setDataAtualizacao(LocalDateTime.now());
        ticketRepository.save(ticket);

        ResolucaoTicket resolucao = ResolucaoTicket.builder()
                .ticket(ticket)
                .funcionario(funcionario)
                .build();

        return resolucaoTicketRepository.save(resolucao);
    }

    /**
     * Método para resolver um ticket já assumido.
     */
    public ResolucaoTicket resolverTicket(ResolucaoTicketRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets em andamento podem ser resolvidos.");
        }

        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(dto.getIdTicket(), dto.getIdFuncionario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket."));

        ticket.setStatus(StatusTicket.CONCLUIDO);
        ticket.setDataAtualizacao(LocalDateTime.now());
        ticketRepository.save(ticket);

        resolucao.setDescricao(dto.getDescricao());
        resolucao.setDataResolucao(LocalDateTime.now());

        return resolucaoTicketRepository.save(resolucao);
    }

    /**
     * Método para reprovar um ticket já assumido.
     */
    public ResolucaoTicket reprovarTicket(Integer idTicket, Integer idFuncionario) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets em andamento podem ser reprovados.");
        }

        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(idTicket, idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket."));

        ticket.setStatus(StatusTicket.REPROVADO);
        ticket.setDataAtualizacao(LocalDateTime.now());
        ticketRepository.save(ticket);

        resolucao.setDescricao("Ticket fake/incoerente");
        resolucao.setDataResolucao(LocalDateTime.now());

        return resolucaoTicketRepository.save(resolucao);
    }

    /**
     * Método para renunciar um ticket já assumido e voltá-lo para PENDENTE.
     */
    public void renunciarTicket(Integer idTicket, Integer idFuncionario) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));
        if (ticket.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets em andamento podem ser renunciados.");
        }

        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(idTicket, idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket."));

        resolucaoTicketRepository.delete(resolucao);
        ticket.setStatus(StatusTicket.PENDENTE);
        ticket.setDataAtualizacao(LocalDateTime.now());
        ticketRepository.save(ticket);
    }
}
