package com.helpbus.HelpBus.model.mapper;

import com.helpbus.HelpBus.model.dto.PessoaDTO;
import com.helpbus.HelpBus.model.entity.Pessoa;

public class PessoaMapper {

    public static Pessoa toEntity(PessoaDTO dto) {
        return Pessoa.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .dataNascimento(dto.getDataNascimento())
                .telefone(dto.getTelefone())
                .cargo(dto.getCargo())
                .dataCadastro(dto.getDataCadastro())
                .build();
    }

    public static PessoaDTO toDTO(Pessoa pessoa) {
        return PessoaDTO.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .dataNascimento(pessoa.getDataNascimento())
                .telefone(pessoa.getTelefone())
                .cargo(pessoa.getCargo())
                .dataCadastro(pessoa.getDataCadastro())
                .build();
    }
}
