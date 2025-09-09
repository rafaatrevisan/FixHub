package com.fixhub.FixHub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resolucao_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucaoTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "data_resolucao", nullable = false)
    private LocalDateTime dataResolucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Pessoa funcionario; // Pessoa que resolveu o ticket
}
