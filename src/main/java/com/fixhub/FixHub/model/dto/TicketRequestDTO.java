package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDTO {
    private Integer idUsuario;
    private String andar;
    private String localizacao;
    private String descricaoLocalizacao;
    private String descricaoTicketUsuario;
    private String imagem;
}
