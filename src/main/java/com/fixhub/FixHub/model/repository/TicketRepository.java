package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer>,
        JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByTicketMestreId(Integer ticketMestreId);
}
