package com.fixhub.FixHub.service;

import com.fixhub.FixHub.exception.BusinessException;
import com.fixhub.FixHub.model.dto.FuncionarioRequestDTO;
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
public class FuncionarioService {

    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    public List<PessoaResponseDTO> listarFuncionariosComFiltros(
            String nome,
            Cargo cargo,
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
                (root, query, cb) -> cb.notEqual(root.get("cargo"), Cargo.CLIENTE)
        );

        if (nome != null && !nome.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
        }

        if (cargo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cargo"), cargo));
        }

        if (telefone != null && !telefone.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("telefone"), "%" + telefone + "%"));
        }

        if (ativo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        }

        if (dataInicioCadastro != null && dataFimCadastro != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("dataCadastro"), dataInicioCadastro, dataFimCadastro));
        } else if (dataInicioCadastro != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataCadastro"), dataInicioCadastro));
        } else if (dataFimCadastro != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dataCadastro"), dataFimCadastro));
        }

        List<Pessoa> lista = pessoaRepository.findAll(spec);

        if (email != null && !email.isBlank()) {
            lista = lista.stream()
                    .filter(p -> usuarioRepository.findByPessoaId(p.getId())
                            .map(u -> u.getEmail().toLowerCase().contains(email.toLowerCase()))
                            .orElse(false))
                    .collect(Collectors.toList());
        }

        return lista.stream()
                .map(pessoa -> {
                    Usuario usuario = usuarioRepository.findByPessoaId(pessoa.getId()).orElse(null);
                    return PessoaMapper.toResponseDTO(pessoa, usuario);
                })
                .collect(Collectors.toList());
    }

    public PessoaResponseDTO cadastrarFuncionario(FuncionarioRequestDTO dto) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();
        if (!authUtil.usuarioTemCargo(Cargo.GERENTE.name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente GERENTE pode cadastrar funcionários");
        }
        if (dto.getCargo() == Cargo.GERENTE && usuarioLogado.getCargo() != Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não permitido cadastrar funcionário com cargo GERENTE");
        }
        validarCadastroFuncionario(dto);
        Pessoa novoFuncionario = Pessoa.builder()
                .nome(dto.getNome())
                .dataNascimento(dto.getDataNascimento())
                .telefone(dto.getTelefone())
                .cargo(dto.getCargo())
                .ativo(true)
                .build();
        pessoaRepository.save(novoFuncionario);
        Usuario usuario = new Usuario(novoFuncionario, dto.getEmail(), passwordEncoder.encode(dto.getSenha()));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        return PessoaMapper.toResponseDTO(novoFuncionario, usuario);
    }

    public PessoaResponseDTO editarFuncionario(Integer id, FuncionarioRequestDTO dto) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();
        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem editar funcionários");
        }
        Pessoa funcionario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
        if (usuarioLogado.getCargo() == Cargo.SUPORTE && funcionario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode alterar dados de um GERENTE");
        }
        validarEdicaoFuncionario(dto);
        funcionario.setNome(dto.getNome());
        funcionario.setDataNascimento(dto.getDataNascimento());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setCargo(dto.getCargo());
        funcionario.setDataAlteracao(LocalDateTime.now());
        funcionario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(funcionario);
        Usuario usuario = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário do funcionário não encontrado"));
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usuario.setEmail(dto.getEmail());
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        usuarioRepository.save(usuario);
        return PessoaMapper.toResponseDTO(funcionario, usuario);
    }

    public void desativarFuncionario(Integer id) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();

        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem desativar funcionários");
        }

        Pessoa funcionario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        if (usuarioLogado.getCargo() == Cargo.SUPORTE && funcionario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode desativar um GERENTE");
        }

        if (!funcionario.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionário já está inativo");
        }

        funcionario.setAtivo(false);
        funcionario.setDataAlteracao(LocalDateTime.now());
        funcionario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(funcionario);

        Usuario usuario = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário do funcionário não encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void reativarFuncionario(Integer id) {
        Pessoa usuarioLogado = authUtil.getPessoaUsuarioLogado();

        if (!(authUtil.usuarioTemCargo(Cargo.GERENTE.name()) || authUtil.usuarioTemCargo(Cargo.SUPORTE.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas GERENTE ou SUPORTE podem reativar funcionários");
        }

        Pessoa funcionario = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        if (usuarioLogado.getCargo() == Cargo.SUPORTE && funcionario.getCargo() == Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPORTE não pode reativar um GERENTE");
        }

        if (funcionario.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionário já está ativo");
        }

        funcionario.setAtivo(true);
        funcionario.setDataAlteracao(LocalDateTime.now());
        funcionario.setUsuarioAlterador(authUtil.getIdPessoaUsuarioLogado());
        pessoaRepository.save(funcionario);

        Usuario usuario = usuarioRepository.findByPessoaId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário do funcionário não encontrado"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private void validarCadastroFuncionario(FuncionarioRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new BusinessException("O campo nome é obrigatório");
        }
        if (dto.getTelefone() == null || dto.getTelefone().isBlank()) {
            throw new BusinessException("O campo telefone é obrigatório");
        }
        if (!ValidationUtil.isTelefoneValido(dto.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (!Pattern.matches("^\\d{10,11}$", dto.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (dto.getDataNascimento() == null) {
            throw new BusinessException("A data de nascimento é obrigatória");
        }
        if (dto.getDataNascimento().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de nascimento deve estar no passado");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException("O campo email é obrigatório");
        }
        if (!ValidationUtil.isEmailValido(dto.getEmail())) {
            throw new BusinessException("O campo email é inválido. Informe um endereço de e‑mail válido.");
        }
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new BusinessException("O campo senha é obrigatório");
        }
        if (dto.getCargo() == null || dto.getCargo() == Cargo.CLIENTE) {
            throw new BusinessException("O cargo do funcionário é inválido");
        }
    }

    private void validarEdicaoFuncionario(FuncionarioRequestDTO dto) {
        if (dto.getNome() != null && dto.getNome().isBlank()) {
            throw new BusinessException("O campo nome não pode estar vazio");
        }
        if (dto.getTelefone() != null && !Pattern.matches("^\\d{10,11}$", dto.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (!ValidationUtil.isTelefoneValido(dto.getTelefone())) {
            throw new BusinessException("O telefone deve ter 10 ou 11 dígitos e conter apenas números");
        }
        if (dto.getDataNascimento() != null && dto.getDataNascimento().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de nascimento deve estar no passado");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException("O campo email é obrigatório");
        }
        if (!ValidationUtil.isEmailValido(dto.getEmail())) {
            throw new BusinessException("O campo email é inválido. Informe um endereço de e‑mail válido.");
        }
        if (dto.getCargo() != null && dto.getCargo() == Cargo.CLIENTE) {
            throw new BusinessException("O cargo do funcionário é inválido");
        }
    }

}