package com.helpbus.HelpBus.model.dto;

import com.helpbus.HelpBus.model.enums.Cargo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PessoaDTO {
    private Integer id;
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private Cargo cargo;
    private LocalDateTime dataCadastro;
}
