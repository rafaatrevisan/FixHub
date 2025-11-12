package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.StatusTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketMestreRepository extends JpaRepository<TicketMestre, Integer>,
        JpaSpecificationExecutor<TicketMestre> {

    @Query("""
        SELECT t 
        FROM TicketMestre t 
        WHERE t.dataCriacaoTicket >= :dataInicio
          AND t.status NOT IN (:status1, :status2)
    """)
    List<TicketMestre> findTicketsMestreUltimas24h(LocalDateTime dataInicio, StatusTicket status1, StatusTicket status2);

    @Query("SELECT t.status, COUNT(t) FROM TicketMestre t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();

    @Query("SELECT t.prioridade, COUNT(t) FROM TicketMestre t GROUP BY t.prioridade")
    List<Object[]> countTicketsByPrioridade();

    @Query("SELECT t.equipeResponsavel, COUNT(t) FROM TicketMestre t GROUP BY t.equipeResponsavel")
    List<Object[]> countTicketsByEquipe();

    @Query("SELECT COUNT(t) FROM TicketMestre t WHERE t.status = :status")
    Long countTicketsByStatusValue(StatusTicket status);

    /**
     * Total de tickets criados no período (para comparações)
     */
    @Query("SELECT COUNT(t) FROM TicketMestre t WHERE t.dataCriacaoTicket BETWEEN :inicio AND :fim")
    Long countTicketsCriadosNoPeriodo(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    /**
     * Tickets pendentes (para cálculo de SLA)
     */
    @Query("SELECT COUNT(t) FROM TicketMestre t WHERE t.status = 'PENDENTE'")
    Long countTicketsPendentes();

    /**
     * Tickets por prioridade (para métricas de SLA)
     */
    @Query("SELECT t.prioridade, COUNT(t) FROM TicketMestre t WHERE t.status = 'RESOLVIDO' GROUP BY t.prioridade")
    List<Object[]> countTicketsResolvidosPorPrioridade();
}
