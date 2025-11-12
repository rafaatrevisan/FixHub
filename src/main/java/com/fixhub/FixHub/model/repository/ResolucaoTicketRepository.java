package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResolucaoTicketRepository extends JpaRepository<ResolucaoTicket, Integer> {

    Optional<ResolucaoTicket> findByTicketIdAndFuncionarioId(Integer ticketId, Integer funcionarioId);

    Optional<ResolucaoTicket> findByTicketId(Integer ticketMestreId);

    /**
     * Tempo médio de resolução (em minutos)
     * CORREÇÃO: A ordem deve ser (data_criacao, data_resolucao)
     */
    @Query(value = """
        SELECT AVG(TIMESTAMPDIFF(MINUTE, t.data_criacao_ticket, r.data_resolucao))
        FROM ticket_mestre t
        JOIN resolucao_ticket r ON r.id_ticket_mestre = t.id
        WHERE t.status = 'CONCLUIDO' AND r.data_resolucao IS NOT NULL
          AND r.data_resolucao > t.data_criacao_ticket
        """, nativeQuery = true)
    Double averageResolutionTime();

    /**
     * Tickets resolvidos por funcionário
     */
    @Query(value = """
        SELECT r.id_funcionario, f.nome, f.cargo, COUNT(r.id) AS total_resolvidos
        FROM resolucao_ticket r
        JOIN pessoa f ON r.id_funcionario = f.id
        GROUP BY r.id_funcionario, f.nome, f.cargo
        ORDER BY total_resolvidos DESC
        """, nativeQuery = true)
    List<Object[]> countTicketsResolvidosPorFuncionario();

    /**
     * Tempo médio de resolução por funcionário (em minutos)
     * CORREÇÃO: A ordem deve ser (data_criacao, data_resolucao)
     */
    @Query(value = """
        SELECT 
            r.id_funcionario, 
            f.nome, 
            AVG(TIMESTAMPDIFF(MINUTE, t.data_criacao_ticket, r.data_resolucao)) AS media_minutos
        FROM resolucao_ticket r
        JOIN ticket_mestre t ON r.id_ticket_mestre = t.id
        JOIN pessoa f ON r.id_funcionario = f.id
        WHERE t.status = 'CONCLUIDO' 
          AND r.data_resolucao IS NOT NULL
          AND r.data_resolucao > t.data_criacao_ticket
        GROUP BY r.id_funcionario, f.nome
        ORDER BY media_minutos ASC
        """, nativeQuery = true)
    List<Object[]> averageResolutionTimePorFuncionario();

    /**
     * Tickets resolvidos no período
     */
    @Query(value = """
        SELECT COUNT(r.id)
        FROM resolucao_ticket r
        WHERE r.data_resolucao BETWEEN :inicio AND :fim
        """, nativeQuery = true)
    Long countTicketsResolvidosNoPeriodo(LocalDateTime inicio, LocalDateTime fim);
}