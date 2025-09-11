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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {
    private Integer id;
    private LocalDateTime dataTicket;
    private LocalDateTime dataAtualizacao;
    private Integer idUsuario;
    private StatusTicket status;
    private PrioridadeTicket prioridade;
    private EquipeResponsavel equipeResponsavel;
    private String andar;
    private String localizacao;
    private String descricaoLocalizacao;
    private String descricaoTicketUsuario;
    private String imagem;
}
