package com.helpbus.HelpBus.service;

import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    public Pessoa salvarPessoa(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public List<Pessoa> listarPessoas() {
        return pessoaRepository.findAll();
    }

    public Optional<Pessoa> buscarPessoaPorId(Integer id) {
        return pessoaRepository.findById(id);
    }

    public void deletarPessoa(Integer id) {
        pessoaRepository.deleteById(id);
    }
}