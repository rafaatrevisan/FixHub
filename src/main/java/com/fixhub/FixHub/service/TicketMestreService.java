package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.dto.TicketMestreDetalhesDTO;
import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.ResolucaoTicket;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.repository.ResolucaoTicketRepository;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import com.fixhub.FixHub.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketMestreService {

    private final TicketMestreRepository ticketMestreRepository;
    private final ResolucaoTicketRepository resolucaoTicketRepository;
    private final AuthUtil authUtil;

    public List<TicketMestre> listarTicketsMestreComFiltros(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusTicket status,
            PrioridadeTicket prioridade,
            String andar
    ) {
        Pessoa funcionario = authUtil.getPessoaUsuarioLogado();

        if (!authUtil.isUsuarioFuncionario()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas funcionários podem acessar esta funcionalidade");
        }

        EquipeResponsavel equipeFuncionario;
        try {
            equipeFuncionario = EquipeResponsavel.valueOf(String.valueOf(funcionario.getCargo()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cargo do funcionário inválido para filtragem");
        }

        Specification<TicketMestre> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("equipeResponsavel"), equipeFuncionario)
        );

        if (dataInicio != null && dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("dataCriacaoTicket"), dataInicio, dataFim));
        } else if (dataInicio != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataCriacaoTicket"), dataInicio));
        } else if (dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dataCriacaoTicket"), dataFim));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (prioridade != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("prioridade"), prioridade));
        }

        if (andar != null && !andar.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("andar"), andar));
        }

        return ticketMestreRepository.findAll(spec);
    }

    public TicketMestreDetalhesDTO buscarTicketMestrePorId(Integer idTicketMestre) {
        Pessoa funcionario = authUtil.getPessoaUsuarioLogado();

        if (!authUtil.isUsuarioFuncionario()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas funcionários podem acessar esta funcionalidade");
        }

        TicketMestre ticketMestre = ticketMestreRepository.findById(idTicketMestre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket Mestre não encontrado"));

        EquipeResponsavel equipeFuncionario;
        try {
            equipeFuncionario = EquipeResponsavel.valueOf(String.valueOf(funcionario.getCargo()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cargo do funcionário inválido para filtragem");
        }

        if (ticketMestre.getEquipeResponsavel() != equipeFuncionario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket pertence a outra equipe. Acesso negado.");
        }

        // Buscar informações da resolução, se existir
        Optional<ResolucaoTicket> resolucaoOpt = resolucaoTicketRepository.findByTicketId(idTicketMestre);

        TicketMestreDetalhesDTO dto = TicketMestreDetalhesDTO.builder()
                .idTicketMestre(ticketMestre.getId())
                .dataCriacaoTicket(ticketMestre.getDataCriacaoTicket())
                .dataAtualizacao(ticketMestre.getDataAtualizacao())
                .status(ticketMestre.getStatus())
                .prioridade(ticketMestre.getPrioridade())
                .equipeResponsavel(ticketMestre.getEquipeResponsavel())
                .andar(ticketMestre.getAndar())
                .localizacao(ticketMestre.getLocalizacao())
                .descricaoLocalizacao(ticketMestre.getDescricaoLocalizacao())
                .descricaoTicketUsuario(ticketMestre.getDescricaoTicketUsuario())
                .imagem(ticketMestre.getImagem())
                .build();

        if (resolucaoOpt.isPresent()) {
            ResolucaoTicket resolucao = resolucaoOpt.get();
            dto.setNomeFuncionario(resolucao.getFuncionario().getNome());
            dto.setDescricaoResolucao(resolucao.getDescricao());
            dto.setDataResolucao(resolucao.getDataResolucao());
        }

        return dto;
    }

}