package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.model.dto.PessoaDTO;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.mapper.PessoaMapper;
import com.helpbus.HelpBus.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PessoaDTO> listarTodos() {
        return pessoaService.listarTodos()
                .stream()
                .map(PessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public PessoaDTO buscarPorId(@PathVariable Integer id) {
        return PessoaMapper.toDTO(pessoaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PessoaDTO> criar(@RequestBody PessoaDTO dto) {
        Pessoa pessoa = PessoaMapper.toEntity(dto);
        Pessoa salva = pessoaService.criarPessoa(pessoa);
        return ResponseEntity.ok(PessoaMapper.toDTO(salva));
    }

    @PutMapping("{id}")
    public ResponseEntity<PessoaDTO> atualizar(@PathVariable Integer id, @RequestBody PessoaDTO dto) {
        Pessoa pessoaAtualizada = PessoaMapper.toEntity(dto);
        Pessoa atualizada = pessoaService.atualizarPessoa(id, pessoaAtualizada);
        return ResponseEntity.ok(PessoaMapper.toDTO(atualizada));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        pessoaService.deletarPessoa(id);
    }
}
