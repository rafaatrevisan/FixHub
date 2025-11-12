package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.RelatorioTicketsDTO;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final AuthUtil authUtil;

    /**
     * Gera relatório detalhado dos tickets com filtros
     */
    public List<RelatorioTicketsDTO> gerarRelatorioTickets(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusTicket status,
            PrioridadeTicket prioridade,
            String equipe,
            String funcionario
    ) {
        var usuario = authUtil.getPessoaUsuarioLogado();

        if (!(usuario.getCargo().name().equals("GERENTE") || usuario.getCargo().name().equals("SUPORTE"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem gerar relatórios.");
        }

        Specification<TicketMestre> spec = Specification.where(null);

        if (dataInicio != null && dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("dataCriacaoTicket"), dataInicio, dataFim));
        } else if (dataInicio != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataCriacaoTicket"), dataInicio));
        } else if (dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dataCriacaoTicket"), dataFim));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (prioridade != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("prioridade"), prioridade));
        }

        if (equipe != null && !equipe.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("equipeResponsavel"), EquipeResponsavel.valueOf(equipe)));
        }

        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        return tickets.stream().map(ticket -> {
            ResolucaoTicket resolucao = resolucaoTicketRepository.findByTicketId(ticket.getId()).orElse(null);
            String nomeFuncionario = resolucao != null ? resolucao.getFuncionario().getNome() : null;
            LocalDateTime dataResolucao = resolucao != null ? resolucao.getDataResolucao() : null;

            return RelatorioTicketsDTO.builder()
                    .id(ticket.getId())
                    .descricao(ticket.getDescricaoTicketUsuario())
                    .status(ticket.getStatus())
                    .prioridade(ticket.getPrioridade())
                    .equipeResponsavel(ticket.getEquipeResponsavel())
                    .dataCriacao(ticket.getDataCriacaoTicket())
                    .dataResolucao(dataResolucao)
                    .funcionarioResponsavel(nomeFuncionario)
                    .build();
        }).collect(Collectors.toList());
    }

}
