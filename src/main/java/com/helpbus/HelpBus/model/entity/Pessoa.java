package com.helpbus.HelpBus.model.entity;

import com.helpbus.HelpBus.model.enums.Cargo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Column(name = "id_cargo")
    private int idCargo;

    @Transient  // Indica que este método não deve ser persistido
    public Cargo getCargo() {
        return Cargo.fromId(idCargo);
    }

    public void setCargo(Cargo cargo) {
        this.idCargo = cargo.getId();
    }
}
