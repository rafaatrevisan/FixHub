package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fixhub/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> getDadosUsuarioLogado() {
        try {
            Pessoa pessoa = authUtil.getPessoaUsuarioLogado();
            Usuario usuario = authUtil.getUsuarioLogado();

            Map<String, Object> dados = new HashMap<>();
            dados.put("id", usuario.getId());
            dados.put("email", usuario.getEmail());
            dados.put("pessoa", Map.of(
                    "id", pessoa.getId(),
                    "nome", pessoa.getNome(),
                    "cargo", pessoa.getCargo(),
                    "telefone", pessoa.getTelefone()
            ));

            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não autenticado"));
        }
    }
}
