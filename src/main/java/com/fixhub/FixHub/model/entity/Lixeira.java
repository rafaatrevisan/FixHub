package com.fixhub.FixHub.model.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "lixeira")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lixeira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String andar;

    @Column(nullable = false, length = 255)
    private String localizacao;

    @Column(nullable = false, length = 255)
    private String descricaoLocalizacao;

    @Column(nullable = false, length = 255)
    private String descricaoTicketUsuario;
}
