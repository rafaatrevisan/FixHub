package com.fixhub.FixHub.service;

import com.fixhub.FixHub.exception.BusinessException;
import com.fixhub.FixHub.model.dto.RegisterRequestDTO;
import com.fixhub.FixHub.model.dto.RegisterResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.UsuarioRepository;
import com.fixhub.FixHub.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    public final ValidationUtil validationUtil;

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email já está em uso");
        }

        Pessoa pessoa = Pessoa.builder()
                .nome(request.getNome())
                .dataNascimento(request.getDataNascimento())
                .telefone(request.getTelefone())
                .cargo(Cargo.CLIENTE)
                .build();

        validationUtil.validarPessoa(pessoa, request.getSenha());

        pessoa = pessoaRepository.save(pessoa);

        Usuario usuario = new Usuario(
                pessoa,
                request.getEmail(),
                passwordEncoder.encode(request.getSenha())
        );
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        return new RegisterResponseDTO(
                "Usuário registrado com sucesso",
                usuario.getId(),
                usuario.getEmail()
        );
    }
}