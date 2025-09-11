package com.fixhub.FixHub.model.dto;

import com.fixhub.FixHub.model.enums.Cargo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PessoaRequestDTO {
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private Cargo cargo;
}
