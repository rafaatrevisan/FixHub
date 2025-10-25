package com.fixhub.FixHub.util;

import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtém o email do usuário logado
     */
    public String getEmailUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return authentication.getName();
    }

    /**
     * Obtém o Usuario completo do usuário logado
     */
    public Usuario getUsuarioLogado() {
        String email = getEmailUsuarioLogado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Obtém o ID do usuário logado
     */
    public Integer getIdUsuarioLogado() {
        return getUsuarioLogado().getId();
    }

    /**
     * Obtém a Pessoa relacionada ao usuário logado
     */
    public Pessoa getPessoaUsuarioLogado() {
        Usuario usuario = getUsuarioLogado();
        return usuario.getPessoa();
    }

    /**
     * Obtém o ID da Pessoa relacionada ao usuário logado
     */
    public Integer getIdPessoaUsuarioLogado() {
        return getPessoaUsuarioLogado().getId();
    }

    /**
     * Verifica se o usuário logado tem um cargo específico
     */
    public boolean usuarioTemCargo(String cargo) {
        Pessoa pessoa = getPessoaUsuarioLogado();
        return pessoa.getCargo().name().equals(cargo);
    }

    /**
     * Verifica se o usuário logado é funcionário (não cliente)
     */
    public boolean isUsuarioFuncionario() {
        return !usuarioTemCargo("CLIENTE");
    }
}
