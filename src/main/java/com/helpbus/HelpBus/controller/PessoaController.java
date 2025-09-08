package com.helpbus.HelpBus.controller;

import com.helpbus.HelpBus.exception.BusinessException;
import com.helpbus.HelpBus.model.dto.PessoaDTO;
import com.helpbus.HelpBus.model.entity.Pessoa;
import com.helpbus.HelpBus.model.mapper.PessoaMapper;
import com.helpbus.HelpBus.model.repository.PessoaRepository;
import com.helpbus.HelpBus.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixhub/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaRepository pessoaRepository;
    private final PessoaService pessoaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PessoaDTO> listarTodos() {
        return pessoaRepository.findAll()
                .stream()
                .map(PessoaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> criar(@RequestBody PessoaDTO dto) {
        try {
            Pessoa pessoa = PessoaMapper.toEntity(dto);
            pessoaService.validarPessoa(pessoa);
            Pessoa salvo = pessoaRepository.save(pessoa);
            return ResponseEntity.ok(PessoaMapper.toDTO(salvo));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoaRepository.delete(pessoa);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody PessoaDTO dto) {
        try {
            pessoaRepository.findById(id)
                    .map(pessoa -> {
                        pessoa.setNome(dto.getNome());
                        pessoa.setDataNascimento(dto.getDataNascimento());
                        pessoa.setTelefone(dto.getTelefone());
                        pessoa.setCargo(dto.getCargo());
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
    public PessoaDTO buscarPorId(@PathVariable Integer id) {
        return pessoaRepository.findById(id)
                .map(PessoaMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }
}
