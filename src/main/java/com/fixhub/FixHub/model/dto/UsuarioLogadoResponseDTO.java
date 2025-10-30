package com.fixhub.FixHub.model.dto;

import com.fixhub.FixHub.model.enums.Cargo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioLogadoResponseDTO {
    private String email;
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
}
