package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.PessoaRequestDTO;
import com.fixhub.FixHub.model.dto.PessoaResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.mapper.PessoaMapper;
import com.fixhub.FixHub.service.PessoaService;
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
    public List<PessoaResponseDTO> listarTodos() {
        return pessoaService.listarTodos()
                .stream()
                .map(PessoaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public PessoaResponseDTO buscarPorId(@PathVariable Integer id) {
        return PessoaMapper.toResponseDTO(pessoaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PessoaResponseDTO> criar(@RequestBody PessoaRequestDTO dto) {
        Pessoa pessoa = PessoaMapper.toEntity(dto);
        Pessoa salva = pessoaService.criarPessoa(pessoa);
        return ResponseEntity.ok(PessoaMapper.toResponseDTO(salva));
    }

    @PutMapping("{id}")
    public ResponseEntity<PessoaResponseDTO> atualizar(@PathVariable Integer id, @RequestBody PessoaRequestDTO dto) {
        Pessoa pessoaAtualizada = PessoaMapper.toEntity(dto);
        Pessoa atualizada = pessoaService.atualizarPessoa(id, pessoaAtualizada);
        return ResponseEntity.ok(PessoaMapper.toResponseDTO(atualizada));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        pessoaService.deletarPessoa(id);
    }
}
