package com.helpbus.HelpBus.model.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa", referencedColumnName = "id")
    private Pessoa pessoa;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "senha", length = 255)
    private String senha;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Construtor customizado sem ID e data_cadastro (para criação)
    public Usuario(Pessoa pessoa, String email, String senha) {
        this.pessoa = pessoa;
        this.email = email;
        this.senha = senha;
    }

    // Lifecycle callback para definir data_cadastro automaticamente
    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }
}
