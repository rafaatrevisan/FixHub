package com.helpbus.HelpBus.model.repository;

import com.helpbus.HelpBus.model.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {
}
