package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {

    List<Pessoa> findByAtivoTrue();

    @Query("SELECT p FROM Pessoa p JOIN Usuario u ON p.id = u.pessoa.id WHERE u.email = :email")
    Optional<Pessoa> findByEmailUsuario(String email);

}
