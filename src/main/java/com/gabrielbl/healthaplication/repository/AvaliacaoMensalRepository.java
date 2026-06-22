package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoMensalRepository extends JpaRepository<AvaliacaoMensal, UUID>{


    Page<AvaliacaoMensal> findByEmpresa(Empresa empresa, Pageable pageable);


    AvaliacaoMensal findByCompetenciaAndEmpresaIdAndIsActive(String competencia, UUID empresaId, boolean isActive);

    AvaliacaoMensal findByEmpresaAndIsActive(Empresa empresa, boolean isActive);

    Optional<AvaliacaoMensal> findFirstByEmpresaAndIsActiveOrderByCreatedAtDesc(Empresa empresa, boolean b);


}
