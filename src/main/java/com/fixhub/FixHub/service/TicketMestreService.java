package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMestreService {

    private final TicketMestreRepository ticketMestreRepository;

    public List<TicketMestre> listarTicketsMestre() {
        return ticketMestreRepository.findAll();
    }
}
