package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.ResolucaoTicketRequestDTO;
import com.fixhub.FixHub.model.entity.*;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.*;
import com.fixhub.FixHub.util.AuthUtil;
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
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogTicketFakeRepository logTicketFakeRepository;
    private final AuthUtil authUtil;

    /**
     * Assumir um TicketMestre e iniciar o trabalho.
     */
    public ResolucaoTicket assumirTicket(Integer idTicketMestre) {
        Pessoa funcionarioLogado = authUtil.getPessoaUsuarioLogado();

        TicketMestre mestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser assumidos.");
        }

        if (funcionarioLogado.getCargo() == Cargo.CLIENTE) {
            throw new IllegalStateException("Usuário não tem permissão para assumir tickets.");
        }

        mestre.setStatus(StatusTicket.EM_ANDAMENTO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.EM_ANDAMENTO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);
        }

        ResolucaoTicket resolucao = ResolucaoTicket.builder()
                .ticket(mestre)
                .funcionario(funcionarioLogado)
                .descricao("")
                .build();

        return resolucaoTicketRepository.save(resolucao);
    }


    /**
     * Renunciar um TicketMestre e voltar para PENDENTE.
     */
    public void renunciarTicket(Integer idTicketMestre) {
        Pessoa funcionarioLogado = authUtil.getPessoaUsuarioLogado();

        TicketMestre mestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets em andamento podem ser renunciados.");
        }

        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(mestre.getId(), funcionarioLogado.getId())
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
        Pessoa funcionarioLogado = authUtil.getPessoaUsuarioLogado();

        TicketMestre mestre = ticketMestreRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets mestres em andamento podem ser resolvidos.");
        }

        // Verifica se o funcionário logado é quem assumiu o ticket
        ResolucaoTicket resolucaoExistente = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(mestre.getId(), funcionarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket mestre."));

        mestre.setStatus(StatusTicket.CONCLUIDO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.CONCLUIDO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);
        }

        // Atualiza a resolução existente
        resolucaoExistente.setDescricao(dto.getDescricao());
        resolucaoExistente.setDataResolucao(LocalDateTime.now());

        return resolucaoTicketRepository.save(resolucaoExistente);
    }

    public ResolucaoTicket reprovarTicket(ResolucaoTicketRequestDTO dto) {
        Pessoa funcionarioLogado = authUtil.getPessoaUsuarioLogado();

        TicketMestre mestre = ticketMestreRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        if (mestre.getStatus() != StatusTicket.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente tickets mestres em andamento podem ser reprovados.");
        }

        ResolucaoTicket resolucaoExistente = resolucaoTicketRepository
                .findByTicketIdAndFuncionarioId(mestre.getId(), funcionarioLogado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não assumiu este ticket mestre."));

        // Busca um dos tickets vinculados para saber quem é o autor do chamado
        Ticket ticketOriginal = ticketRepository.findFirstByTicketMestreId(mestre.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum ticket encontrado para este ticket mestre."));

        Pessoa usuarioQueCriou = ticketOriginal.getUsuario();

        int novosFakes = usuarioQueCriou.getTicketsFakes() + 1;
        usuarioQueCriou.setTicketsFakes(novosFakes);

        LogTicketFake log = LogTicketFake.builder()
                .pessoa(usuarioQueCriou)
                .ticketMestre(mestre)
                .funcionario(funcionarioLogado)
                .motivo("Ticket reprovado por improcedência. Motivo: " + dto.getDescricao())
                .dataRegistro(LocalDateTime.now())
                .build();

        logTicketFakeRepository.save(log);

        // Se chegou a 5 ou mais → desativar pessoa + desativar usuário
        if (novosFakes >= 5) {
            usuarioQueCriou.setAtivo(false);

            usuarioRepository.findByPessoaId(usuarioQueCriou.getId())
                    .ifPresent(u -> {
                        u.setAtivo(false);
                        usuarioRepository.save(u);
                    });
        }

        pessoaRepository.save(usuarioQueCriou);


        mestre.setStatus(StatusTicket.REPROVADO);
        mestre.setDataAtualizacao(LocalDateTime.now());
        ticketMestreRepository.save(mestre);

        List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());
        for (Ticket t : ticketsVinculados) {
            t.setStatus(StatusTicket.REPROVADO);
            t.setDataAtualizacao(LocalDateTime.now());
            ticketRepository.save(t);
        }

        resolucaoExistente.setDescricao(dto.getDescricao());
        resolucaoExistente.setDataResolucao(LocalDateTime.now());

        return resolucaoTicketRepository.save(resolucaoExistente);
    }
}
