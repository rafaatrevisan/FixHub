package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.DashboardResumoDTO;
import com.fixhub.FixHub.model.dto.GraficoTicketsDTO;
import com.fixhub.FixHub.model.dto.DesempenhoFuncionarioDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final AuthUtil authUtil;

    /**
     * Retorna um resumo geral dos tickets para o dashboard
     */
    public DashboardResumoDTO getResumo() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }

        Long total = ticketMestreRepository.count();
        Long abertos = ticketMestreRepository.countTicketsByStatusValue(StatusTicket.PENDENTE);
        Long andamento = ticketMestreRepository.countTicketsByStatusValue(StatusTicket.EM_ANDAMENTO);
        Long resolvidos = ticketMestreRepository.countTicketsByStatusValue(StatusTicket.CONCLUIDO);
        Double tempoMedio = resolucaoTicketRepository.averageResolutionTime();

        double percentualSLA = 0.0;
        if (total != null && total > 0) {
            percentualSLA = ((double) resolvidos / total) * 100;
        }

        return DashboardResumoDTO.builder()
                .totalTickets(total)
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
    public List<GraficoTicketsDTO> getTicketsPorStatus() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }

        return ticketMestreRepository.countTicketsByStatus()
                .stream()
                .map(obj -> new GraficoTicketsDTO(obj[0].toString(), (Long) obj[1]))
                .collect(Collectors.toList());
    }

    /**
     * Retorna gráfico de tickets por prioridade
     */
    public List<GraficoTicketsDTO> getTicketsPorPrioridade() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }

        return ticketMestreRepository.countTicketsByPrioridade()
                .stream()
                .map(obj -> new GraficoTicketsDTO(obj[0].toString(), (Long) obj[1]))
                .collect(Collectors.toList());
    }

    /**
     * Retorna gráfico de tickets por equipe responsável
     */
    public List<GraficoTicketsDTO> getTicketsPorEquipe() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }

        return ticketMestreRepository.countTicketsByEquipe()
                .stream()
                .map(obj -> new GraficoTicketsDTO(obj[0].toString(), (Long) obj[1]))
                .collect(Collectors.toList());
    }

    /**
     * Retorna ranking de desempenho dos funcionários
     */
    public List<DesempenhoFuncionarioDTO> getDesempenhoFuncionarios() {
        Pessoa usuario = authUtil.getPessoaUsuarioLogado();
        if (!(usuario.getCargo() == Cargo.GERENTE || usuario.getCargo() == Cargo.SUPORTE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a GERENTE ou SUPORTE");
        }

        List<Object[]> resolvidosPorFuncionario = resolucaoTicketRepository.countTicketsResolvidosPorFuncionario();

        List<Object[]> tempoMedioPorFuncionario = resolucaoTicketRepository.averageResolutionTimePorFuncionario();

        return resolvidosPorFuncionario.stream().map(obj -> {
            Integer idFuncionario = (Integer) obj[0];
            String nomeFuncionario = (String) obj[1];
            Long totalTickets = (Long) obj[2];

            // Encontra o tempo médio correspondente
            Double tempoMedio = tempoMedioPorFuncionario.stream()
                    .filter(t -> ((Integer) t[0]).equals(idFuncionario))
                    .map(t -> (Double) t[2])
                    .findFirst()
                    .orElse(0.0);

            return DesempenhoFuncionarioDTO.builder()
                    .idFuncionario(idFuncionario)
                    .nomeFuncionario(nomeFuncionario)
                    .cargo("FUNCIONARIO")
                    .ticketsResolvidos(totalTickets)
                    .tempoMedioResolucao(tempoMedio)
                    .build();
        }).collect(Collectors.toList());
    }
}
