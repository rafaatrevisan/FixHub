package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.ResolucaoTicketRequestDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import com.fixhub.FixHub.model.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolucaoTicketService {

    private final TicketRepository ticketRepository;
    private final TicketMestreRepository ticketMestreRepository;
    private final PessoaRepository pessoaRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;

    /**
     * Assumir um TicketMestre e iniciar o trabalho.
     */
    public ResolucaoTicket assumirTicket(Integer idTicketMestre, Integer idFuncionario) {
        TicketMestre mestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser assumidos.");
        }

        Pessoa funcionario = pessoaRepository.findById(idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        mestre.setStatus(StatusTicket.EM_ANDAMENTO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        ResolucaoTicket ultimaResolucao = null;

        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.EM_ANDAMENTO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);

            ResolucaoTicket resolucao = ResolucaoTicket.builder()
                    .ticket(mestre)
                    .funcionario(funcionario)
                    .descricao("")
                    .build();

            ultimaResolucao = resolucaoTicketRepository.save(resolucao);
        }
        return ultimaResolucao;
    }

    /**
     * Renunciar um TicketMestre e voltar para PENDENTE.
     */
    public void renunciarTicket(Integer idTicketMestre, Integer idFuncionario) {
        TicketMestre mestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets em andamento podem ser renunciados.");
        }

        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(mestre.getId(), idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket mestre."));

        resolucaoTicketRepository.delete(resolucao);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.PENDENTE);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);
        }

        mestre.setStatus(StatusTicket.PENDENTE);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);
    }

    /**
     * Resolver um TicketMestre já assumido.
     */
    public ResolucaoTicket resolverTicket(ResolucaoTicketRequestDTO dto) {
        TicketMestre mestre = ticketMestreRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets mestres em andamento podem ser resolvidos.");
        }

        mestre.setStatus(StatusTicket.CONCLUIDO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.CONCLUIDO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);

            ResolucaoTicket resolucao = resolucaoTicketRepository
                    .findByTicketIdAndFuncionarioId(mestre.getId(), dto.getIdFuncionario())
                    .orElse(ResolucaoTicket.builder()
                            .ticket(mestre)
                            .funcionario(pessoaRepository.findById(dto.getIdFuncionario())
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado")))
                            .descricao("") 
                            .build());

            resolucao.setDescricao(dto.getDescricao());
            resolucao.setDataResolucao(LocalDateTime.now());
            resolucaoTicketRepository.save(resolucao);
        }

        return resolucaoTicketRepository.findByTicketIdAndFuncionarioId(mestre.getId(), dto.getIdFuncionario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar resolução"));
    }

    /**
     * Reprovar um TicketMestre.
     */
    public ResolucaoTicket reprovarTicket(Integer idTicketMestre, Integer idFuncionario) {
        TicketMestre mestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets mestres em andamento podem ser reprovados.");
        }

        mestre.setStatus(StatusTicket.REPROVADO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.REPROVADO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);

            ResolucaoTicket resolucao = resolucaoTicketRepository
                    .findByTicketIdAndFuncionarioId(mestre.getId(), idFuncionario)
                    .orElse(ResolucaoTicket.builder()
                            .ticket(mestre)
                            .funcionario(pessoaRepository.findById(idFuncionario)
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado")))
                            .descricao("")
                            .build());

            resolucao.setDescricao("Ticket fake/incoerente");
            resolucao.setDataResolucao(LocalDateTime.now());
            resolucaoTicketRepository.save(resolucao);
        }

        return resolucaoTicketRepository.findByTicketIdAndFuncionarioId(mestre.getId(), idFuncionario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar reprovação"));
    }
}