package com.fixhub.FixHub.controller;

import com.fixhub.FixHub.model.dto.RegisterRequestDTO;
import com.fixhub.FixHub.model.dto.RegisterResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.repository.UsuarioRepository;
import com.fixhub.FixHub.security.JwtUtil;
import com.fixhub.FixHub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fixhub")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Login padrão (clientes e todos os usuários)
     * Retorna token, nome, cargo e informações do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String senha) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Pessoa pessoa = usuario.getPessoa();
            Cargo cargo = pessoa.getCargo();

            if (!usuario.getAtivo() || !pessoa.isAtivo()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Conta desativada. Entre em contato com o suporte.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            String token = jwtUtil.generateToken(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login realizado com sucesso");
            response.put("nome", pessoa.getNome());
            response.put("cargo", cargo.toString());
            response.put("email", email);
            response.put("isAdmin", cargo == Cargo.GERENTE || cargo == Cargo.SUPORTE);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciais inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Login administrativo (apenas GERENTE e SUPORTE)
     * Valida permissões de acesso ao painel admin
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestParam String email, @RequestParam String senha) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Pessoa pessoa = usuario.getPessoa();
            Cargo cargo = pessoa.getCargo();

            if (!usuario.getAtivo() || !pessoa.isAtivo()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Conta desativada. Entre em contato com o suporte.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            if (cargo != Cargo.GERENTE && cargo != Cargo.SUPORTE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acesso negado. Apenas GERENTES e SUPORTE podem acessar o painel administrativo.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            String token = jwtUtil.generateToken(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login administrativo realizado com sucesso");
            response.put("nome", pessoa.getNome());
            response.put("cargo", cargo.toString());
            response.put("email", email);
            response.put("isAdmin", true);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciais inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Registro de novo usuário
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            RegisterResponseDTO response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
