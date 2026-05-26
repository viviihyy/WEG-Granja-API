package com.weg.granja.repository;

import com.weg.granja.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    Optional<Colaborador> findByMatricula(String matricula);

    Optional<Colaborador> findByMatriculaAndAtivoTrue(String matricula);

    boolean existsByMatricula(String matricula);

    List<Colaborador> findAllByAtivoTrue();
}
