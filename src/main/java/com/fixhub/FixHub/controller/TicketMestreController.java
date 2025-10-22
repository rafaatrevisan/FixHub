package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.TicketMestreResponseDTO;
import com.fixhub.FixHub.model.mapper.TicketMestreMapper;
import com.fixhub.FixHub.service.TicketMestreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/tickets-mestre")
@RequiredArgsConstructor
public class TicketMestreController {

    private final TicketMestreService ticketMestreService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TicketMestreResponseDTO> listarTodosTicketsMestre() {
        return ticketMestreService.listarTicketsMestre()
                .stream()
                .map(TicketMestreMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
