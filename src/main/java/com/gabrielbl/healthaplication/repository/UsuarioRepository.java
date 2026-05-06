package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Usuario findByLogin(String login);
    List<Usuario> findBySetorId(UUID setorId);
    Usuario findByLoginAndEmpresa(String login, Empresa empresa);


}
