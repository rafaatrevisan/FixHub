package com.fixhub.FixHub.model.mapper;

import com.fixhub.FixHub.model.dto.PessoaRequestDTO;
import com.fixhub.FixHub.model.dto.PessoaResponseDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.Usuario;

public class PessoaMapper {

    public static Pessoa toEntity(PessoaRequestDTO dto) {
        return Pessoa.builder()
                .nome(dto.getNome())
                .dataNascimento(dto.getDataNascimento())
                .telefone(dto.getTelefone())
                .cargo(dto.getCargo())
                .build();
    }

    public static PessoaResponseDTO toResponseDTO(Pessoa pessoa) {
        return PessoaResponseDTO.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .dataNascimento(pessoa.getDataNascimento())
                .telefone(pessoa.getTelefone())
                .cargo(pessoa.getCargo())
                .dataCadastro(pessoa.getDataCadastro())
                .ativo(pessoa.getAtivo())
                .dataAlteracao(pessoa.getDataAlteracao())
                .usuarioAlterador(pessoa.getUsuarioAlterador())
                .build();
    }

    public static PessoaResponseDTO toResponseDTO(Pessoa pessoa, Usuario usuario) {
        return PessoaResponseDTO.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .dataNascimento(pessoa.getDataNascimento())
                .telefone(pessoa.getTelefone())
                .cargo(pessoa.getCargo())
                .dataCadastro(pessoa.getDataCadastro())
                .ativo(pessoa.getAtivo())
                .dataAlteracao(pessoa.getDataAlteracao())
                .usuarioAlterador(pessoa.getUsuarioAlterador())
                .email(usuario != null ? usuario.getEmail() : null)
                .build();
    }
}
