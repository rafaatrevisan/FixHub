package com.helpbus.HelpBus.model.mapper;

import com.helpbus.HelpBus.model.dto.TicketDTO;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.entity.Ticket;

public class TicketMapper {

    public static Ticket toEntity(TicketDTO dto, Pessoa usuario) {
        return Ticket.builder()
                .id(dto.getId())
                .dataTicket(dto.getDataTicket())
                .usuario(usuario)
                .status(dto.getStatus())
                .prioridade(dto.getPrioridade())
                .andar(dto.getAndar())
                .localizacao(dto.getLocalizacao())
                .descricaoLocalizacao(dto.getDescricaoLocalizacao())
                .descricaoTicketUsuario(dto.getDescricaoTicketUsuario())
                .imagem(dto.getImagem())
                .build();
    }

    public static TicketDTO toDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .dataTicket(ticket.getDataTicket())
                .idUsuario(ticket.getUsuario().getId())
                .status(ticket.getStatus())
                .prioridade(ticket.getPrioridade())
                .andar(ticket.getAndar())
                .localizacao(ticket.getLocalizacao())
                .descricaoLocalizacao(ticket.getDescricaoLocalizacao())
                .descricaoTicketUsuario(ticket.getDescricaoTicketUsuario())
                .imagem(ticket.getImagem())
                .build();
    }
}
