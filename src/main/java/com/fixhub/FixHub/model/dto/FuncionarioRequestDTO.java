package com.fixhub.FixHub.model.dto;

import com.fixhub.FixHub.model.enums.Cargo;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FuncionarioRequestDTO {
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String senha;
    private Cargo cargo;
}
