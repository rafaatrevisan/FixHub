package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer>,
        JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByTicketMestreId(Integer ticketMestreId);

    Optional<Ticket> findFirstByTicketMestreId(Integer ticketMestreId);

}
