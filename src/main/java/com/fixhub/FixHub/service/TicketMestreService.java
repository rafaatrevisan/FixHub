package com.fixhub.FixHub.service;

import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import com.fixhub.FixHub.model.enums.StatusTicket;
import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.repository.PessoaRepository;
import com.fixhub.FixHub.model.repository.TicketMestreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMestreService {

    private final TicketMestreRepository ticketMestreRepository;
    private final PessoaRepository pessoaRepository;

    public List<TicketMestre> listarTicketsMestreComFiltros(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            StatusTicket status,
            PrioridadeTicket prioridade,
            String andar
    ) {
        // 🔹 Pega o usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = auth.getName();

        Pessoa funcionario = pessoaRepository.findByEmailUsuario(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

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
}
