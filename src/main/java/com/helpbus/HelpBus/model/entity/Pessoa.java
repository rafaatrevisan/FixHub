package com.helpbus.HelpBus.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(name = "data_nascimento", updatable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 11)
    private String telefone;

    //TODO: precisa mapear a entidade id_cargo e adicionar validações nos campos (NotEmpty, NotBlank)
}
