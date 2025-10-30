package com.fixhub.FixHub.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotNull(message = "Data de nascimento é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos")
    @Pattern(regexp = "\\d+", message = "Telefone deve conter apenas números")
    private String telefone;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;
}
