package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Pessoa;
import com.fixhub.FixHub.model.enums.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer>, JpaSpecificationExecutor<Pessoa> {

    List<Pessoa> findByAtivoTrue();

    List<Pessoa> findByAtivoTrueAndCargoNot(Cargo cargo);

    @Query("SELECT p FROM Pessoa p JOIN Usuario u ON p.id = u.pessoa.id WHERE u.email = :email")
    Optional<Pessoa> findByEmailUsuario(String email);

}
