package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.PessoaRequestDTO;
import com.fixhub.FixHub.model.dto.PessoaResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.mapper.PessoaMapper;
import com.fixhub.FixHub.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixhub/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/filtro")
    public ResponseEntity<List<PessoaResponseDTO>> listarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicioCadastro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFimCadastro
    ) {
        List<PessoaResponseDTO> lista = usuarioService.listarUsuariosComFiltros(
                nome, email, telefone, ativo, dataInicioCadastro, dataFimCadastro
        );
        return ResponseEntity.ok(lista);
    }

    @PatchMapping("{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id) {
        usuarioService.desativarUsuario(id);
    }

    @PatchMapping("{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(@PathVariable Integer id) {
        usuarioService.reativarUsuario(id);
    }
}