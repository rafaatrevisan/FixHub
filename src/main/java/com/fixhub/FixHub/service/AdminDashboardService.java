package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.DashboardResumoDTO;
import com.fixhub.FixHub.model.dto.GraficoTicketsDTO;
import com.fixhub.FixHub.model.dto.DesempenhoFuncionarioDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final AuthUtil authUtil;

    /**
     * Valida permissões de acesso
     */
    private void validarAcesso() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }
    }

    /**
     * Cria Specification para filtrar por data
     */
    private Specification<TicketMestre> criarFiltroData(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataCriacaoTicket"), dataInicio));
            }

            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataCriacaoTicket"), dataFim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Retorna um resumo geral dos tickets para o dashboard
     */
    public DashboardResumoDTO getResumo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        validarAcesso();

        Specification<TicketMestre> spec = criarFiltroData(dataInicio, dataFim);
        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        long total = tickets.size();
        long abertos = tickets.stream().filter(t -> t.getStatus() == StatusTicket.PENDENTE).count();
        long andamento = tickets.stream().filter(t -> t.getStatus() == StatusTicket.EM_ANDAMENTO).count();
        long resolvidos = tickets.stream().filter(t -> t.getStatus() == StatusTicket.CONCLUIDO || t.getStatus() == StatusTicket.REPROVADO).count();

        // Tempo médio de resolução (no período)
        Double tempoMedio = resolucaoTicketRepository.averageResolutionTimeNoPeriodo(dataInicio, dataFim);

        double percentualSLA = 0.0;
        if (total > 0) {
            percentualSLA = Math.round(((double) resolvidos / total) * 10000.0) / 100.0;
        }

        return DashboardResumoDTO.builder()
                .totalTickets((long) total)
                .ticketsAbertos(abertos)
                .ticketsEmAndamento(andamento)
                .ticketsResolvidos(resolvidos)
                .tempoMedioResolucao(tempoMedio != null ? tempoMedio : 0.0)
                .percentualSLA(percentualSLA)
                .build();
    }

    /**
     * Retorna gráfico de tickets por status
     */
    public List<GraficoTicketsDTO> getTicketsPorStatus(LocalDateTime dataInicio, LocalDateTime dataFim) {
        validarAcesso();

        Specification<TicketMestre> spec = criarFiltroData(dataInicio, dataFim);
        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        Map<StatusTicket, Long> contagem = tickets.stream()
                .collect(Collectors.groupingBy(TicketMestre::getStatus, Collectors.counting()));

        return contagem.entrySet().stream()
                .map(entry -> new GraficoTicketsDTO(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Retorna gráfico de tickets por prioridade
     */
    public List<GraficoTicketsDTO> getTicketsPorPrioridade(LocalDateTime dataInicio, LocalDateTime dataFim) {
        validarAcesso();

        Specification<TicketMestre> spec = criarFiltroData(dataInicio, dataFim);
        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        Map<String, Long> contagem = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPrioridade().toString(), Collectors.counting()));

        return contagem.entrySet().stream()
                .map(entry -> new GraficoTicketsDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Retorna gráfico de tickets por equipe responsável
     */
    public List<GraficoTicketsDTO> getTicketsPorEquipe(LocalDateTime dataInicio, LocalDateTime dataFim) {
        validarAcesso();

        Specification<TicketMestre> spec = criarFiltroData(dataInicio, dataFim);
        List<TicketMestre> tickets = ticketMestreRepository.findAll(spec);

        Map<String, Long> contagem = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getEquipeResponsavel().toString(), Collectors.counting()));

        return contagem.entrySet().stream()
                .map(entry -> new GraficoTicketsDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Retorna ranking de desempenho dos funcionários
     */
    public List<DesempenhoFuncionarioDTO> getDesempenhoFuncionarios(LocalDateTime dataInicio, LocalDateTime dataFim) {
        validarAcesso();

        List<Object[]> resolvidosPorFuncionario = resolucaoTicketRepository.countTicketsResolvidosPorFuncionarioNoPeriodo(dataInicio, dataFim);
        List<Object[]> tempoMedioPorFuncionario = resolucaoTicketRepository.averageResolutionTimePorFuncionarioNoPeriodo(dataInicio, dataFim);

        return resolvidosPorFuncionario.stream().map(obj -> {
                    Integer idFuncionario = ((Number) obj[0]).intValue();
                    String nomeFuncionario = (String) obj[1];
                    String cargoFuncionario = (String) obj[2];
                    Long totalTickets = ((Number) obj[3]).longValue();

                    Double tempoMedio = tempoMedioPorFuncionario.stream()
                            .filter(t -> ((Number) t[0]).intValue() == idFuncionario)
                            .map(t -> {
                                Double tempo = ((Number) t[2]).doubleValue();
                                return tempo != null ? Math.abs(tempo) : 0.0;
                            })
                            .findFirst()
                            .orElse(0.0);

                    return DesempenhoFuncionarioDTO.builder()
                            .idFuncionario(idFuncionario)
                            .nomeFuncionario(nomeFuncionario)
                            .cargo(cargoFuncionario)
                            .ticketsResolvidos(totalTickets)
                            .tempoMedioResolucao(tempoMedio)
                            .build();
                })
                .sorted((a, b) -> Double.compare(a.getTempoMedioResolucao(), b.getTempoMedioResolucao()))
                .collect(Collectors.toList());
    }
}