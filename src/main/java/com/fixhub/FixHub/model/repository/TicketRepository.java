package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByTicketMestreId(Integer ticketMestreId);
}
