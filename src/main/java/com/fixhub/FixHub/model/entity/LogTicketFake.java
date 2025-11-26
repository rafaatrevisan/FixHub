package com.fixhub.FixHub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "log_ticket_fake")
public class LogTicketFake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_pessoa", nullable = false)
    private Pessoa pessoa;

    @ManyToOne
    @JoinColumn(name = "id_ticket_mestre", nullable = false)
    private TicketMestre ticketMestre;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Pessoa funcionario;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(nullable = false, length = 255)
    private String motivo;

    @PrePersist
    protected void onCreate() {
        this.dataRegistro = LocalDateTime.now();
    }
}
