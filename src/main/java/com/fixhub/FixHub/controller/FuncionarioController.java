package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.FuncionarioRequestDTO;
import com.fixhub.FixHub.model.dto.PessoaResponseDTO;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping("/filtro")
    public ResponseEntity<List<PessoaResponseDTO>> listarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Cargo cargo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicioCadastro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFimCadastro
    ) {
        List<PessoaResponseDTO> lista = funcionarioService.listarFuncionariosComFiltros(
                nome, cargo, email, telefone, ativo, dataInicioCadastro, dataFimCadastro
        );
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@RequestBody FuncionarioRequestDTO dto) {
        PessoaResponseDTO novo = funcionarioService.cadastrarFuncionario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("{id}")
    public ResponseEntity<PessoaResponseDTO> editar(
            @PathVariable Integer id,
            @RequestBody FuncionarioRequestDTO dto
    ) {
        PessoaResponseDTO editado = funcionarioService.editarFuncionario(id, dto);
        return ResponseEntity.ok(editado);
    }

    @PatchMapping("{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id) {
        funcionarioService.desativarFuncionario(id);
    }

    @PatchMapping("{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(@PathVariable Integer id) {
        funcionarioService.reativarFuncionario(id);
    }
}