package com.fixhub.FixHub.model.mapper;

import com.fixhub.FixHub.model.dto.ResolucaoTicketResponseDTO;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;

public class ResolucaoTicketMapper {

    public static ResolucaoTicketResponseDTO toDTO(ResolucaoTicket resolucao) {
        return ResolucaoTicketResponseDTO.builder()
                .id(resolucao.getId())
                .idTicket(resolucao.getTicket().getId())
                .idFuncionario(resolucao.getFuncionario().getId())
                .descricao(resolucao.getDescricao())
                .dataResolucao(resolucao.getDataResolucao())
                .statusTicket(resolucao.getTicket().getStatus())
                .build();
    }
}
