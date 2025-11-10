package com.fixhub.FixHub.util;

import com.fixhub.FixHub.exception.BusinessException;
import com.fixhub.FixHub.model.entity.Pessoa;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String TELEFONE_REGEX = "^\\d{10,11}$";
    private static final Pattern TELEFONE_PATTERN = Pattern.compile(TELEFONE_REGEX);

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Valida se o telefone contém apenas números e tem 10 ou 11 dígitos.
     */
    public static boolean isTelefoneValido(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return false;
        }
        return TELEFONE_PATTERN.matcher(telefone).matches();
    }

    /**
     * Valida se o e‑mail tem formato válido.
     */
    public static boolean isEmailValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
