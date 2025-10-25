package com.fixhub.FixHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private String message;
    private Integer usuarioId;
    private String email;
}
