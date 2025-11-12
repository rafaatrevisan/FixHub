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

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final AuthUtil authUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            try {
                EquipeResponsavel equipeEnum = EquipeResponsavel.valueOf(equipe.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("equipeResponsavel"), equipeEnum));
            } catch (IllegalArgumentException e) {
            }
        }

        if (funcionario != null && !funcionario.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<ResolucaoTicket> resolucaoRoot = subquery.from(ResolucaoTicket.class);
                subquery.select(resolucaoRoot.get("ticket").get("id"))
                        .where(cb.like(cb.lower(resolucaoRoot.get("funcionario").get("nome")),
                                "%" + funcionario.toLowerCase() + "%"));

                return cb.in(root.get("id")).value(subquery);
            });
        }

        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        return tickets.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte TicketMestre para RelatorioTicketsDTO
     */
    private RelatorioTicketsDTO converterParaDTO(TicketMestre ticket) {
        ResolucaoTicket resolucao = resolucaoTicketRepository
                .findByTicketId(ticket.getId())
                .orElse(null);

        String nomeFuncionario = null;
        LocalDateTime dataResolucao = null;
        Long tempoResolucao = null;

        if (resolucao != null) {
            nomeFuncionario = resolucao.getFuncionario() != null
                    ? resolucao.getFuncionario().getNome()
                    : null;
            dataResolucao = resolucao.getDataResolucao();

            // CORREÇÃO: Calcula tempo de resolução em MINUTOS (positivo)
            if (dataResolucao != null && ticket.getDataCriacaoTicket() != null) {
                tempoResolucao = ChronoUnit.MINUTES.between(
                        ticket.getDataCriacaoTicket(),  // Data INÍCIO
                        dataResolucao                    // Data FIM
                );

                // Garantir que o tempo seja positivo
                if (tempoResolucao < 0) {
                    tempoResolucao = Math.abs(tempoResolucao);
                }
            }
        }

        return RelatorioTicketsDTO.builder()
                .id(Long.valueOf(ticket.getId()))
                .descricao(ticket.getDescricaoTicketUsuario())
                .status(ticket.getStatus())
                .prioridade(ticket.getPrioridade())
                .equipeResponsavel(ticket.getEquipeResponsavel())
                .dataCriacao(ticket.getDataCriacaoTicket())
                .dataResolucao(dataResolucao)
                .funcionarioResponsavel(nomeFuncionario)
                .tempoResolucao(tempoResolucao)
                .build();
    }

    /**
     * Exporta relatório de tickets em formato CSV
     */
    public byte[] exportarRelatorioCSV(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusTicket status,
            PrioridadeTicket prioridade,
            String equipe,
            String funcionario
    ) {
        List<RelatorioTicketsDTO> tickets = gerarRelatorioTickets(
                dataInicio, dataFim, status, prioridade, equipe, funcionario
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // BOM para UTF-8
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Cabeçalho
            writer.println("ID;Descrição;Status;Prioridade;Equipe;Funcionário;Data Criação;Data Resolução;Tempo (min)");

            // Dados
            for (RelatorioTicketsDTO ticket : tickets) {
                writer.println(formatarLinhaCSV(ticket));
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao gerar CSV: " + e.getMessage());
        }
    }

    /**
     * Formata linha do CSV
     */
    private String formatarLinhaCSV(RelatorioTicketsDTO ticket) {
        return String.format("%d;%s;%s;%s;%s;%s;%s;%s;%s",
                ticket.getId(),
                escaparCSV(ticket.getDescricao()),
                ticket.getStatus() != null ? ticket.getStatus().name().replace("_", " ") : "",
                ticket.getPrioridade() != null ? ticket.getPrioridade().name() : "",
                ticket.getEquipeResponsavel() != null ? ticket.getEquipeResponsavel().name() : "",
                escaparCSV(ticket.getFuncionarioResponsavel()),
                ticket.getDataCriacao() != null ? ticket.getDataCriacao().format(DATE_FORMATTER) : "",
                ticket.getDataResolucao() != null ? ticket.getDataResolucao().format(DATE_FORMATTER) : "",
                ticket.getTempoResolucao() != null ? ticket.getTempoResolucao().toString() : ""
        );
    }

    /**
     * Escapa caracteres especiais para CSV
     */
    private String escaparCSV(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        valor = valor.replace("\n", " ").replace("\r", " ");
        if (valor.contains(";") || valor.contains("\"")) {
            valor = "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
