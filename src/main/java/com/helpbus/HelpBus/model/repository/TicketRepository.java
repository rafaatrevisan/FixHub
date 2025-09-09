package com.helpbus.HelpBus.model.repository;

import com.helpbus.HelpBus.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
