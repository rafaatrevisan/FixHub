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
}
