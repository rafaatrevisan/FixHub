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

    public void validarPessoa(Pessoa pessoa, String senha) {
        if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
            throw new BusinessException("O campo nome é obrigatório");
        }
        if (pessoa.getTelefone() == null || pessoa.getTelefone().isBlank()) {
            throw new BusinessException("O campo telefone é obrigatório");
        }
        if (!ValidationUtil.isTelefoneValido(pessoa.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (pessoa.getDataNascimento() == null) {
            throw new BusinessException("A data de nascimento é obrigatória");
        }
        if (pessoa.getDataNascimento().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de nascimento deve estar no passado");
        }
        int idade = Period.between(pessoa.getDataNascimento(), LocalDate.now()).getYears();
        if (idade < 16) {
            throw new BusinessException("A pessoa deve ter pelo menos 16 anos");
        }
        if (senha == null || senha.isBlank()) {
            throw new BusinessException("O campo senha é obrigatório");
        }
        if (senha.length() < 6) {
            throw new BusinessException("A senha deve conter no mínimo 6 caracteres");
        }
    }
}
