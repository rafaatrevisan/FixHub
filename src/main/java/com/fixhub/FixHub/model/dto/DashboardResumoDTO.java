package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoDTO {
    private Long totalTickets;
    private Long ticketsAbertos;
    private Long ticketsEmAndamento;
    private Long ticketsResolvidos;
    private Double tempoMedioResolucao; // em minutos
    private Double percentualSLA;
}
