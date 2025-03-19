package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import com.helpbus.HelpBus.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/helpbus/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;

    //TODO: criar try-catch com exception personalizada

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Pessoa> listarTodos() {
        return pessoaRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Define que a resposta será 201 Created
    public Pessoa criar(@RequestBody Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id){
        pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoaRepository.delete(pessoa);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody Pessoa pessoaAtualizada){
        pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoa.setNome(pessoaAtualizada.getNome());
                    pessoa.setDataNascimento(pessoaAtualizada.getDataNascimento());
                    pessoa.setTelefone(pessoaAtualizada.getTelefone());
                    return pessoaRepository.save(pessoa);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @GetMapping("{id}")
    public Pessoa buscarPorId(@PathVariable Integer id){
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }
}
