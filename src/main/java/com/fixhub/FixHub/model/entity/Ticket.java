package com.fixhub.FixHub.model.entity;

import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
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

    @Column(name = "data_criacao_ticket", nullable = false, updatable = false)
    private LocalDateTime dataTicket;

    @Column(name = "data_atualizacao_ticket")
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true)
    private Pessoa usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTicket status;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 20)
    private PrioridadeTicket prioridade;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipe_responsavel", nullable = false, length = 20)
    private EquipeResponsavel equipeResponsavel;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket_mestre")
    private TicketMestre ticketMestre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lixeira", nullable = true)
    private Lixeira lixeira;

    @PrePersist
    protected void onCreate() {
        this.dataTicket = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
