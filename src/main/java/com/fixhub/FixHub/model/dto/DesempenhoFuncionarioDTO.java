package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesempenhoFuncionarioDTO {
    private Integer idFuncionario;
    private String nomeFuncionario;
    private String cargo;
    private Long ticketsResolvidos;
    private Double tempoMedioResolucao; // em minutos
}
