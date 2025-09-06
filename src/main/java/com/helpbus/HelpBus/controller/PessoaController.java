package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.exception.BusinessException;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import com.helpbus.HelpBus.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/fixhub/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PessoaService pessoaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Pessoa> listarTodos() {
        return pessoaRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> criar(@RequestBody Pessoa pessoa) {
        try {
            pessoaService.validarPessoa(pessoa);
            return ResponseEntity.ok(pessoaRepository.save(pessoa));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id){
        pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoaRepository.delete(pessoa);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody Pessoa pessoaAtualizada) {
        try {
            pessoaRepository.findById(id)
                    .map(pessoa -> {
                        pessoa.setNome(pessoaAtualizada.getNome());
                        pessoa.setDataNascimento(pessoaAtualizada.getDataNascimento());
                        pessoa.setTelefone(pessoaAtualizada.getTelefone());
                        pessoa.setCargo(pessoaAtualizada.getCargo());
                        pessoaService.validarPessoa(pessoa);
                        pessoaRepository.save(pessoa);
                        return pessoa;
                    })
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
        } catch (BusinessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("{id}")
    public Pessoa buscarPorId(@PathVariable Integer id){
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }
}
