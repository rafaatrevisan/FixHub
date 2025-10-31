package com.fixhub.FixHub.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String TELEFONE_REGEX = "^\\d{10,11}$";
    private static final Pattern TELEFONE_PATTERN = Pattern.compile(TELEFONE_REGEX);

    /**
     * Valida se o telefone contém apenas números e tem 10 ou 11 dígitos.
     */
    public static boolean isTelefoneValido(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return false;
        }
        return TELEFONE_PATTERN.matcher(telefone).matches();
    }
}
