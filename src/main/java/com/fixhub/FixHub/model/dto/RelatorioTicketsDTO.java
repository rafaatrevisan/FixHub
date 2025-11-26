package com.fixhub.FixHub.model.dto;

import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioTicketsDTO {
    private Long id;
    private String descricao;
    private StatusTicket status;
    private PrioridadeTicket prioridade;
    private EquipeResponsavel equipeResponsavel;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataResolucao;
    private String funcionarioResponsavel;
    private String descricaoResolucao;
    private Long tempoResolucao;
}
