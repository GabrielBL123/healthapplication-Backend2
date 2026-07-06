package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoSetor;
import com.gabrielbl.healthaplication.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, UUID> {


    List<Resposta> findByAvaliacaoSetor(AvaliacaoSetor avaliacaoSetor);

}