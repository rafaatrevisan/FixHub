package com.fixhub.FixHub.model.mapper;

import com.fixhub.FixHub.model.dto.TicketMestreResponseDTO;
import com.fixhub.FixHub.model.entity.TicketMestre;

public class TicketMestreMapper {

    public static TicketMestreResponseDTO toResponseDTO(TicketMestre mestre) {
        return TicketMestreResponseDTO.builder()
                .id(mestre.getId())
                .dataCriacaoTicket(mestre.getDataCriacaoTicket())
                .status(mestre.getStatus())
                .prioridade(mestre.getPrioridade())
                .equipeResponsavel(mestre.getEquipeResponsavel())
                .andar(mestre.getAndar())
                .localizacao(mestre.getLocalizacao())
                .descricaoLocalizacao(mestre.getDescricaoLocalizacao())
                .descricaoTicketUsuario(mestre.getDescricaoTicketUsuario())
                .imagem(mestre.getImagem())
                .build();
    }
}
