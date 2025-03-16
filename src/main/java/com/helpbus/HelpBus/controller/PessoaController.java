package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/helpbus/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    @Autowired
    private PessoaService pessoaService;

    //TODO: criar try-catch com exception personalizada

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Pessoa> listarPessoas() {
        return pessoaService.listarPessoas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Define que a resposta será 201 Created
    public Pessoa criarPessoa(@RequestBody Pessoa pessoa) {
        return pessoaService.salvarPessoa(pessoa);
    }
}