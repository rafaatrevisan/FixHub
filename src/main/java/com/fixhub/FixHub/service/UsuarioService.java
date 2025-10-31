package com.fixhub.FixHub.service;

import com.fixhub.FixHub.exception.BusinessException;
import com.fixhub.FixHub.model.dto.PessoaResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.mapper.PessoaMapper;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.UsuarioRepository;
import com.fixhub.FixHub.util.AuthUtil;
import com.fixhub.FixHub.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista usuários clientes com filtros
     */
    public List<PessoaResponseDTO> listarUsuariosComFiltros(
            String nome,
            String email,
            String telefone,
            Boolean ativo,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicioCadastro,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFimCadastro
    ) {
        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem acessar esta funcionalidade");
        }

        Specification<Pessoa> spec = Specification.<Pessoa>where(
                (root, query, cb) -> cb.equal(root.get("cargo"), Cargo.CLIENTE)
        );

        if (nome != null && !nome.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
        }

        if (telefone != null && !telefone.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("telefone"), "%" + telefone + "%"));
        }

        if (ativo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        }

        if (dataInicioCadastro != null && dataFimCadastro != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("dataCadastro"), dataInicioCadastro, dataFimCadastro));
        } else if (dataInicioCadastro != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataCadastro"), dataInicioCadastro));
        } else if (dataFimCadastro != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataCadastro"), dataFimCadastro));
        }

        List<Pessoa> lista = pessoaRepository.findAll(spec);

        return lista.stream()
                .map(pessoa -> {
                    Usuario usuario = usuarioRepository.findByPessoaId(pessoa.getId()).orElse(null);
                    return PessoaMapper.toResponseDTO(pessoa, usuario);
                })
                .collect(Collectors.toList());
    }

    /**
     * Edita dados do usuário cliente
     */
    public PessoaResponseDTO editarUsuario(Integer id, Pessoa usuarioAtualizado, String email, String senha) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();

        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem editar usuários");
        }

        Pessoa usuario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuarioLogado.getCargo() == Cargo.SUPORTE && usuario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode alterar dados de um GERENTE");
        }

        if (!usuario.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível editar um usuário inativo");
        }

        validarEdicaoUsuario(usuarioAtualizado);

        if (usuarioAtualizado.getNome() != null) usuario.setNome(usuarioAtualizado.getNome());
        if (usuarioAtualizado.getDataNascimento() != null) usuario.setDataNascimento(usuarioAtualizado.getDataNascimento());
        if (usuarioAtualizado.getTelefone() != null) usuario.setTelefone(usuarioAtualizado.getTelefone());

        usuario.setDataAlteracao(LocalDateTime.now());
        usuario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(usuario);

        Usuario usuarioEntity = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro na tabela usuario não encontrado"));

        if (email != null && !email.isBlank()) {
            usuarioEntity.setEmail(email);
        }
        if (senha != null && !senha.isBlank()) {
            usuarioEntity.setSenha(passwordEncoder.encode(senha));
        }
        usuarioRepository.save(usuarioEntity);

        return PessoaMapper.toResponseDTO(usuario, usuarioEntity);
    }

    /**
     * Desativa um usuário cliente
     */
    public void desativarUsuario(Integer id) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();

        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem desativar usuários");
        }

        Pessoa usuario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuarioLogado.getCargo() == Cargo.SUPORTE && usuario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode desativar um GERENTE");
        }

        if (!usuario.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já está inativo");
        }

        usuario.setAtivo(false);
        usuario.setDataAlteracao(LocalDateTime.now());
        usuario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(usuario);

        Usuario usuarioEntity = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro na tabela usuario não encontrado"));
        usuarioEntity.setAtivo(false);
        usuarioRepository.save(usuarioEntity);
    }

    /**
     * Reativa um usuário cliente
     */
    public void reativarUsuario(Integer id) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();

        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem reativar usuários");
        }

        Pessoa usuario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuarioLogado.getCargo() == Cargo.SUPORTE && usuario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode reativar um GERENTE");
        }

        if (usuario.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já está ativo");
        }

        usuario.setAtivo(true);
        usuario.setDataAlteracao(LocalDateTime.now());
        usuario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(usuario);

        Usuario usuarioEntity = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro na tabela usuario não encontrado"));
        usuarioEntity.setAtivo(true);
        usuarioRepository.save(usuarioEntity);
    }

    private void validarEdicaoUsuario(Pessoa usuarioAtualizado) {
        if (usuarioAtualizado.getNome() != null && usuarioAtualizado.getNome().isBlank()) {
            throw new BusinessException("O campo nome não pode estar vazio");
        }
        if (usuarioAtualizado.getTelefone() != null && !ValidationUtil.isTelefoneValido(usuarioAtualizado.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (usuarioAtualizado.getDataNascimento() != null &&
                usuarioAtualizado.getDataNascimento().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de nascimento deve estar no passado");
        }
    }
}