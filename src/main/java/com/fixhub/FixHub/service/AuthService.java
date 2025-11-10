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

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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

        this.validarPessoa(pessoa, request.getSenha());

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


    public void validarPessoa(Pessoa pessoa, String senha) {
        if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
            throw new BusinessException("O campo nome é obrigatório");
        }
        if (pessoa.getTelefone() == null || pessoa.getTelefone().isBlank()) {
            throw new BusinessException("O campo telefone é obrigatório");
        }
        if (!ValidationUtil.isTelefoneValido(pessoa.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (pessoa.getDataNascimento() == null) {
            throw new BusinessException("A data de nascimento é obrigatória");
        }
        if (pessoa.getDataNascimento().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de nascimento deve estar no passado");
        }
        int idade = Period.between(pessoa.getDataNascimento(), LocalDate.now()).getYears();
        if (idade < 16) {
            throw new BusinessException("A pessoa deve ter pelo menos 16 anos");
        }
        if (senha == null || senha.isBlank()) {
            throw new BusinessException("O campo senha é obrigatório");
        }
        if (senha.length() < 6) {
            throw new BusinessException("A senha deve conter no mínimo 6 caracteres");
        }
    }
}