package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.AvaliacaoSetor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AvaliacaoSetorRepository extends JpaRepository<AvaliacaoSetor, UUID> {
    AvaliacaoSetor findBySetorNomeAndAvaliacaoMensal(String nome, AvaliacaoMensal avaliacaoMensal);
}
