package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;


@Service

public class EmpresaService {



    @Autowired
    private EmpresaRepository empresaRepository;

    public void criarEmpresa(RegistrarEmpresaDTO data) {

        if(empresaRepository.findByCnpj(data.cnpj())!=null) throw new AlreadySubmittedException("Cnpj ja registrado.");


        Empresa empresa = new Empresa(data.cnpj(), data.nome(), data.email(), data.telefone());
        empresaRepository.save(empresa);
    }



    public void atualizarEmpresa(UUID id, AtualizarEmpresaDTO data) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa nao encontrada"));

        empresa.setNome(data.nome());
        empresa.setEmail(data.email());
        empresa.setTelefone(data.telefone());

        empresaRepository.save(empresa);



    }

    public void deletarEmpresa(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa nao encontrada"));

        empresaRepository.delete(empresa);
    }


    public List<Empresa> getAllEmpresas() {
        return empresaRepository.findAll();
    }
}
