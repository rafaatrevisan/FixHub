package com.fixhub.FixHub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pessoa", referencedColumnName = "id")
    private Pessoa pessoa;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "senha", length = 255)
    private String senha;

    @Column(nullable = false)
    private Boolean ativo;

    public Usuario(Pessoa pessoa, String email, String senha) {
        this.pessoa = pessoa;
        this.email = email;
        this.senha = senha;
    }

    @PrePersist
    protected void onCreate() {
        this.ativo = true;
    }
}
