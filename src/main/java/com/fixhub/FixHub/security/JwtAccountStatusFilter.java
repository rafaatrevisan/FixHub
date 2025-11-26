package com.fixhub.FixHub.security;

import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccountStatusFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                auth.getName() != null && !auth.getName().equals("anonymousUser")) {

            String email = auth.getName();

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            if (usuario != null) {
                if (!usuario.getAtivo() || !usuario.getPessoa().getAtivo()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\": \"Conta desativada. Entre em contato com o suporte.\"}"
                    );
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
