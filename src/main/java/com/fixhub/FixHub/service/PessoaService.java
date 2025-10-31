package com.fixhub.FixHub.service;

import com.fixhub.FixHub.exception.BusinessException;
import com.fixhub.FixHub.model.dto.UsuarioLogadoResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;
import com.fixhub.FixHub.model.enums.Cargo;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final AuthUtil authUtil;

    public List<Pessoa> listarTodos() {
        return pessoaRepository.findByAtivoTrue();
    }

    public Pessoa buscarPorId(Integer id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));

        if (!pessoa.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa inativa");
        }

        return pessoa;
    }

    public Pessoa criarPessoa(Pessoa pessoa) {
        validarPessoa(pessoa);
        atribuirCargoAutomatico(pessoa);
        return pessoaRepository.save(pessoa);
    }

    public Pessoa atualizarPessoa(Integer id, Pessoa pessoaAtualizada) {
        // Obtém o usuário logado automaticamente
        Pessoa usuarioAlterador = authUtil.getPessoaUsuarioLogado();

        boolean ehGerenteOuSuporte = usuarioAlterador.getCargo() == Cargo.GERENTE || usuarioAlterador.getCargo() == Cargo.SUPORTE;
        boolean ehProprioUsuario = usuarioAlterador.getId().equals(id);

        if (!(ehGerenteOuSuporte || ehProprioUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem permissão para alteração.");
        }

        return pessoaRepository.findById(id)
                .map(pessoa -> {
                    if (!pessoa.isAtivo()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível atualizar uma pessoa inativa");
                    }
                    if (pessoaAtualizada.getNome() != null) pessoa.setNome(pessoaAtualizada.getNome());
                    if (pessoaAtualizada.getDataNascimento() != null) pessoa.setDataNascimento(pessoaAtualizada.getDataNascimento());
                    if (pessoaAtualizada.getTelefone() != null) pessoa.setTelefone(pessoaAtualizada.getTelefone());
                    if (pessoaAtualizada.getCargo() != null) pessoa.setCargo(pessoaAtualizada.getCargo());
                    validarPessoa(pessoa);

                    pessoa.setDataAlteracao(LocalDateTime.now());
                    pessoa.setUsuarioAlterador(usuarioAlterador.getId());
                    return pessoaRepository.save(pessoa);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }

    public void desativarPessoa(Integer id) {
        // Obtém o usuário logado automaticamente
        Pessoa usuarioAlterador = authUtil.getPessoaUsuarioLogado();

        boolean ehGerenteOuSuporte = usuarioAlterador.getCargo() == Cargo.GERENTE || usuarioAlterador.getCargo() == Cargo.SUPORTE;
        boolean ehProprioUsuario = usuarioAlterador.getId().equals(id);

        if (!(ehGerenteOuSuporte || ehProprioUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem permissão para desativação.");
        }

        Pessoa pessoa = buscarPorId(id);
        pessoa.setAtivo(false);
        pessoa.setDataAlteracao(LocalDateTime.now());
        pessoa.setUsuarioAlterador(usuarioAlterador.getId());
        pessoaRepository.save(pessoa);
    }

    public void validarPessoa(Pessoa pessoa) {
        if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
            throw new BusinessException("O campo nome é obrigatório");
        }
        if (pessoa.getTelefone() == null || pessoa.getTelefone().isBlank()) {
            throw new BusinessException("O campo telefone é obrigatório");
        }
        if (!isTelefoneValido(pessoa.getTelefone())) {
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
    }

    private boolean isTelefoneValido(String telefone) {
        String regex = "^\\d{10,11}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(telefone).matches();
    }

    private void atribuirCargoAutomatico(Pessoa pessoa) {
        if (pessoa.getCargo() == null) {
            pessoa.setCargo(Cargo.CLIENTE);
        }
    }

    public UsuarioLogadoResponseDTO getUsuarioLogadoInfo() {
        Usuario usuario = authUtil.getUsuarioLogado();
        Pessoa pessoa = usuario.getPessoa();

        return UsuarioLogadoResponseDTO.builder()
                .email(usuario.getEmail())
                .nome(pessoa.getNome())
                .dataNascimento(pessoa.getDataNascimento())
                .telefone(pessoa.getTelefone())
                .build();
    }

}
