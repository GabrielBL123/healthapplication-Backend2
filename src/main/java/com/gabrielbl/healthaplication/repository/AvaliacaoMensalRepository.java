package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoMensalRepository extends JpaRepository<AvaliacaoMensal, UUID>{


    AvaliacaoMensal findByCompetenciaAndEmpresaId(String  competencia,UUID empresaId);

    AvaliacaoMensal findByCompetenciaAndEmpresaIdAndIsActive(String competencia, UUID empresaId, boolean isActive);
}
