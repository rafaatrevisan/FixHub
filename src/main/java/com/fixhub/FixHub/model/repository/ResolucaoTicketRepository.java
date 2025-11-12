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
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, t.dataCriacaoTicket, r.dataResolucao)) " +
            "FROM TicketMestre t JOIN ResolucaoTicket r ON r.ticket.id = t.id " +
            "WHERE t.status = 'RESOLVIDO'")
    Double averageResolutionTime();
    /**
     * Tickets resolvidos por funcionário
     */
    @Query("SELECT r.funcionario.id, r.funcionario.nome, COUNT(r) " +
            "FROM ResolucaoTicket r GROUP BY r.funcionario.id, r.funcionario.nome")
    List<Object[]> countTicketsResolvidosPorFuncionario();
    /**
     * Tempo médio de resolução por funcionário
     */
    @Query("SELECT r.funcionario.id, r.funcionario.nome, AVG(TIMESTAMPDIFF(MINUTE, t.dataCriacaoTicket, r.dataResolucao)) " +
            "FROM ResolucaoTicket r JOIN TicketMestre t ON r.ticket.id = t.id " +
            "WHERE t.status = 'RESOLVIDO' GROUP BY r.funcionario.id, r.funcionario.nome")
    List<Object[]> averageResolutionTimePorFuncionario();
    /**
     * Tickets resolvidos no período
     */
    @Query("SELECT COUNT(r) FROM ResolucaoTicket r WHERE r.dataResolucao BETWEEN :inicio AND :fim")
    Long countTicketsResolvidosNoPeriodo(LocalDateTime inicio, LocalDateTime fim);
}