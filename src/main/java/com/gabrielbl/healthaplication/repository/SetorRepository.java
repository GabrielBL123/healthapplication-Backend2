package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor,String> {

    Setor findByNomeAndEmpresaCnpj(String nome,String empresaCnpj);
}
