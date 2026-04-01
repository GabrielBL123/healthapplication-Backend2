package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Empresa findByCnpj(String cnpj);

}
