package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetorRepository extends JpaRepository<Setor, UUID> {

    Setor findByNomeAndEmpresaCnpj(String nome, String empresaCnpj);

    Page<Setor> findByEmpresaCnpj(String cnpj, Pageable pageable);
}
