package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResolucaoTicketRepository extends JpaRepository<ResolucaoTicket, Integer> {
}
