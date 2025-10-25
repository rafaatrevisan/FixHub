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

    @PutMapping("{id}")
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestParam Integer idUsuarioAlterador,
            @RequestBody PessoaRequestDTO dto) {
        Pessoa pessoaAtualizada = PessoaMapper.toEntity(dto);
        Pessoa atualizada = pessoaService.atualizarPessoa(id, pessoaAtualizada, idUsuarioAlterador);
        return ResponseEntity.ok(PessoaMapper.toResponseDTO(atualizada));
    }

    @PatchMapping("{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(
            @PathVariable Integer id,
            @RequestParam Integer idUsuarioAlterador) {
        pessoaService.desativarPessoa(id, idUsuarioAlterador);
    }

    @PatchMapping("{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(
            @PathVariable Integer id,
            @RequestParam Integer idUsuarioAlterador) {
        pessoaService.reativarPessoa(id, idUsuarioAlterador);
    }
}
