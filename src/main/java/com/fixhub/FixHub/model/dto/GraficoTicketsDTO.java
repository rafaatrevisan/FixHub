package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraficoTicketsDTO {
    private String categoria; // STATUS
    private Long quantidade;
}
