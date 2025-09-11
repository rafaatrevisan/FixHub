package com.fixhub.FixHub.model.dto;

import com.fixhub.FixHub.model.enums.StatusTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucaoTicketResponseDTO {
    private Integer id;
    private Integer idTicket;
    private Integer idFuncionario;
    private String descricao;
    private LocalDateTime dataResolucao;
    private StatusTicket statusTicket;
}
