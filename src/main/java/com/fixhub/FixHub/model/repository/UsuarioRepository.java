package com.fixhub.FixHub.model.repository;

import com.fixhub.FixHub.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByPessoaId(Integer pessoaId);
}
