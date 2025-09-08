package com.helpbus.HelpBus.model.dto;

import com.helpbus.HelpBus.model.enums.PrioridadeTicket;
import com.helpbus.HelpBus.model.enums.StatusTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Integer id;
    private LocalDateTime dataTicket;
    private Integer idUsuario;
    private StatusTicket status;
    private PrioridadeTicket prioridade;
    private String andar;
    private String localizacao;
    private String descricaoLocalizacao;
    private String descricaoTicketUsuario;
    private String imagem;
}
