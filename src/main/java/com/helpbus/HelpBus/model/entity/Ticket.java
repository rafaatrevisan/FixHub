package com.helpbus.HelpBus.model.entity;

import com.helpbus.HelpBus.model.enums.PrioridadeTicket;
import com.helpbus.HelpBus.model.enums.StatusTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_ticket", nullable = false)
    private LocalDateTime dataTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Pessoa usuario; // Pessoa que abriu o ticket

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTicket status;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 20)
    private PrioridadeTicket prioridade;

    @Column(name = "andar", length = 255)
    private String andar;

    @Column(name = "localizacao", length = 255)
    private String localizacao;

    @Column(name = "descricao_localizacao", length = 255)
    private String descricaoLocalizacao;

    @Column(name = "descricao_ticket_usuario", length = 255)
    private String descricaoTicketUsuario;

    @Column(name = "imagem", length = 255)
    private String imagem;
}
