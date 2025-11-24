package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.TicketDetalhesDTO;
import com.fixhub.FixHub.model.dto.TicketResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.mapper.TicketMapper;
import com.fixhub.FixHub.service.TicketService;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AuthUtil authUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketResponseDTO> criar(
            @RequestParam String andar,
            @RequestParam String localizacao,
            @RequestParam String descricaoLocalizacao,
            @RequestParam String descricaoTicketUsuario,
            @RequestParam(required = false) MultipartFile imagem
    ) {
        try {
            Pessoa usuario = authUtil.getPessoaUsuarioLogado();
            Ticket ticket = ticketService.criarTicket(
                    andar,
                    localizacao,
                    descricaoLocalizacao,
                    descricaoTicketUsuario,
                    imagem,
                    usuario
            );
            return ResponseEntity.ok(TicketMapper.toResponseDTO(ticket));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> atualizar(
            @PathVariable Integer id,
            @RequestParam String andar,
            @RequestParam String localizacao,
            @RequestParam String descricaoLocalizacao,
            @RequestParam String descricaoTicketUsuario,
            @RequestParam(required = false) MultipartFile imagem
    ) {
        try {
            Pessoa usuario = authUtil.getPessoaUsuarioLogado();
            Ticket atualizado = ticketService.atualizarTicket(
                    id,
                    andar,
                    localizacao,
                    descricaoLocalizacao,
                    descricaoTicketUsuario,
                    imagem,
                    usuario
            );
            return ResponseEntity.ok(TicketMapper.toResponseDTO(atualizado));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao processar imagem"));
        }
    }

    @GetMapping("detalhes/{id}")
    public TicketDetalhesDTO buscarDetalhes(@PathVariable Integer id) {
        return ticketService.buscarTicketComDetalhes(id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
    }

    @PostMapping("/lixeira/{idLixeira}")
    public ResponseEntity<String> criarTicketPorLixeira(@PathVariable Integer idLixeira) {
        ticketService.criarTicketPorLixeira(idLixeira);
        return ResponseEntity.ok("Ticket criado");
    }

    @GetMapping("/listar-meus-tickets")
    public ResponseEntity<List<TicketResponseDTO>> listarMeusTicketsComFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) StatusTicket status,
            @RequestParam(required = false) PrioridadeTicket prioridade,
            @RequestParam(required = false) String andar
    ) {
        List<Ticket> meusTickets = ticketService.listarMeusTicketsComFiltros(
                dataInicio, dataFim, status, prioridade, andar
        );

        List<TicketResponseDTO> response = meusTickets.stream()
                .map(TicketMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}