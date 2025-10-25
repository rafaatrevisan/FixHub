package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.TicketDetalhesDTO;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final PessoaRepository pessoaRepository;
    private final GeminiService geminiService;

    public Ticket criarTicket(Ticket ticket, Integer idUsuario) {
        Pessoa usuario = pessoaRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        ticket.setUsuario(usuario);
        ticket.setStatus(StatusTicket.PENDENTE);

        LocalDateTime inicio = LocalDateTime.now().minusHours(24);
        List<TicketMestre> ticketsMestreRecentes = ticketMestreRepository
                .findTicketsMestreUltimas24h(inicio, StatusTicket.CONCLUIDO, StatusTicket.REPROVADO);

        Object resultadoComparacao = geminiService.compararComListaTicketsMestre(ticket, ticketsMestreRecentes);

        if (resultadoComparacao instanceof Integer idMestre) {
            TicketMestre mestre = ticketMestreRepository.findById(idMestre)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

            ticket.setTicketMestre(mestre);

            ticket.setPrioridade(mestre.getPrioridade());
            ticket.setEquipeResponsavel(mestre.getEquipeResponsavel());
            ticket.setStatus(mestre.getStatus());

        } else {
            GeminiService.GeminiResult resultadoIA = geminiService.avaliarTicket(
                    ticket.getDescricaoTicketUsuario(),
                    ticket.getLocalizacao(),
                    ticket.getDescricaoLocalizacao(),
                    ticket.getAndar()
            );

            TicketMestre novoMestre = TicketMestre.builder()
                    .status(StatusTicket.PENDENTE)
                    .prioridade(resultadoIA.prioridade())
                    .equipeResponsavel(resultadoIA.equipeResponsavel())
                    .andar(ticket.getAndar())
                    .localizacao(ticket.getLocalizacao())
                    .descricaoLocalizacao(ticket.getDescricaoLocalizacao())
                    .descricaoTicketUsuario(ticket.getDescricaoTicketUsuario())
                    .imagem(ticket.getImagem())
                    .build();

            ticketMestreRepository.save(novoMestre);
            ticket.setTicketMestre(novoMestre);

            ticket.setPrioridade(resultadoIA.prioridade());
            ticket.setEquipeResponsavel(resultadoIA.equipeResponsavel());
        }

        return ticketRepository.save(ticket);
    }

    public Ticket atualizarTicket(Integer id, Ticket ticketAtualizado, Integer idUsuario) {
        return ticketRepository.findById(id)
                .map(ticketExistente -> {
                    if (ticketExistente.getStatus() != StatusTicket.PENDENTE) {
                        throw new IllegalStateException("O ticket deve estar pendente para ser atualizado");
                    }

                    boolean mesmoProblema = geminiService.mesmoProblema(ticketExistente, ticketAtualizado);
                    if (!mesmoProblema) {
                        throw new IllegalStateException(
                                "Erro ao atualizar ticket! Para reportar um problema diferente, " +
                                        "você deve excluir este ticket já existente e abrir outro."
                        );
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

    // Método para que um usuário possa excluir um ticket que ele abriu sem querer ou que tenha informações erradas (apenas se ainda estiver pendente)
    public void deleteTicket(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (ticket.getStatus() != StatusTicket.PENDENTE) {
            throw new IllegalStateException("Somente tickets pendentes podem ser excluídos");
        }

        TicketMestre mestre = ticket.getTicketMestre();
        if (mestre != null) {
            List<Ticket> ticketsVinculados = ticketRepository.findByTicketMestreId(mestre.getId());

            if (ticketsVinculados.size() == 1) {
                ticketRepository.delete(ticket);
                ticketMestreRepository.delete(mestre);
                return;
            }
        }
        ticketRepository.delete(ticket);
    }


    public TicketDetalhesDTO buscarTicketComResolucao(Integer idTicket, Integer idUsuario) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket não encontrado"));

        if (!ticket.getUsuario().getId().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este ticket não pertence ao usuário informado.");
        }

        if (ticket.getStatus() == StatusTicket.PENDENTE) {
            throw new IllegalStateException("Ticket pendente não possui resolução.");
        }

        TicketMestre ticketMestre = ticket.getTicketMestre();
        if (ticketMestre == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket não possui ticket mestre vinculado.");
        }

        Integer idTicketMestre = ticketMestre.getId();

        Optional<ResolucaoTicket> resolucaoOpt = resolucaoTicketRepository.findByTicketId(idTicketMestre);

        TicketDetalhesDTO dto = TicketDetalhesDTO.builder()
                .idTicket(ticket.getId())
                .idUsuario(ticket.getUsuario().getId())
                .nomeUsuario(ticket.getUsuario().getNome())
                .dataTicket(ticket.getDataTicket())
                .dataAtualizacao(ticket.getDataAtualizacao())
                .status(ticket.getStatus())
                .prioridade(ticket.getPrioridade())
                .equipeResponsavel(ticket.getEquipeResponsavel())
                .andar(ticket.getAndar())
                .localizacao(ticket.getLocalizacao())
                .descricaoLocalizacao(ticket.getDescricaoLocalizacao())
                .descricaoTicketUsuario(ticket.getDescricaoTicketUsuario())
                .imagem(ticket.getImagem())
                .build();

        if (resolucaoOpt.isPresent()) {
            ResolucaoTicket resolucao = resolucaoOpt.get();
            dto.setNomeFuncionario(resolucao.getFuncionario().getNome());
            dto.setDescricaoResolucao(resolucao.getDescricao());
            dto.setDataResolucao(resolucao.getDataResolucao());
        }

        return dto;
    }

}
