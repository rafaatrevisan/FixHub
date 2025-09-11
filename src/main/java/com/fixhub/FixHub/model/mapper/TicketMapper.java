package com.fixhub.FixHub.model.mapper;

import com.fixhub.FixHub.model.dto.TicketRequestDTO;
import com.fixhub.FixHub.model.dto.TicketResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Ticket;

public class TicketMapper {

    public static Ticket toEntity(TicketRequestDTO dto, Pessoa usuario) {
        return Ticket.builder()
                .usuario(usuario)
                .andar(dto.getAndar())
                .localizacao(dto.getLocalizacao())
                .descricaoLocalizacao(dto.getDescricaoLocalizacao())
                .descricaoTicketUsuario(dto.getDescricaoTicketUsuario())
                .imagem(dto.getImagem())
                .build();
    }

    public static TicketResponseDTO toResponseDTO(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .dataTicket(ticket.getDataTicket())
                .dataAtualizacao(ticket.getDataAtualizacao())
                .idUsuario(ticket.getUsuario().getId())
                .status(ticket.getStatus())
                .prioridade(ticket.getPrioridade())
                .equipeResponsavel(ticket.getEquipeResponsavel())
                .andar(ticket.getAndar())
                .localizacao(ticket.getLocalizacao())
                .descricaoLocalizacao(ticket.getDescricaoLocalizacao())
                .descricaoTicketUsuario(ticket.getDescricaoTicketUsuario())
                .imagem(ticket.getImagem())
                .build();
    }
}
