package com.helpbus.HelpBus.service;

import com.helpbus.HelpBus.exception.BusinessException;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.enums.Cargo;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    public void validarPessoa(Pessoa pessoa) {
        if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
            throw new BusinessException("O campo nome é obrigatório");
        }

        if (pessoa.getTelefone() == null || pessoa.getTelefone().isBlank()) {
            throw new BusinessException("O campo telefone é obrigatório");
        }

        if (!isTelefoneValido(pessoa.getTelefone())) {
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

        atribuirCargoAutomatico(pessoa);
    }

    private boolean isTelefoneValido(String telefone) {
        String regex = "^\\d{10,11}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(telefone).matches();
    }

    public void atribuirCargoAutomatico(Pessoa pessoa) {
        if (pessoa.getCargo() == null) {
            pessoa.setCargo(Cargo.CLIENTE);
        }
    }
}
