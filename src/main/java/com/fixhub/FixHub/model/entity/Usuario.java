package com.fixhub.FixHub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa", referencedColumnName = "id")
    private Pessoa pessoa;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "senha", length = 255)
    private String senha;

    public Usuario(Pessoa pessoa, String email, String senha) {
        this.pessoa = pessoa;
        this.email = email;
        this.senha = senha;
    }
}
