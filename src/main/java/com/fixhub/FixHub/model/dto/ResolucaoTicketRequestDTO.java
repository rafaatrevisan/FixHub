package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucaoTicketRequestDTO {
    private Integer idTicket;
    private Integer idFuncionario;
    private String descricao;
}
