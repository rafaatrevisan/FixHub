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
public class TicketDetalhesDTO {
    // Dados do ticket
    private Integer idTicket;
    private String nomeUsuario;
    private Integer idUsuario;
    private LocalDateTime dataTicket;
    private LocalDateTime dataAtualizacao;
    private StatusTicket status;
    private PrioridadeTicket prioridade;
    private EquipeResponsavel equipeResponsavel;
    private String andar;
    private String localizacao;
    private String descricaoLocalizacao;
    private String descricaoTicketUsuario;
    private String imagem;

    // Dados da resolução
    private Integer idResolucao;
    private Integer idFuncionario;
    private String nomeFuncionario;
    private String descricaoResolucao;
    private LocalDateTime dataResolucao;
}
