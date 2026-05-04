package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.AvaliacaoLink;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.AvaliacaoLinkRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service

public class EmpresaService {



    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AvaliacaoLinkRepository avaliacaoLinkRepository;

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

    public String gerarLinkAvaliacao(UUID empresaId, Integer horasExpiracao) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa nao encontrada"));

        // Generate a unique link token
        String linkToken = UUID.randomUUID().toString();

        // Calculate expiry time (optional)
        LocalDateTime expiracaoEm = horasExpiracao != null
                ? LocalDateTime.now().plusHours(horasExpiracao)
                : null;

        // Save the link information in the database
        AvaliacaoLink avaliacaoLink = new AvaliacaoLink();
        avaliacaoLink.setEmpresaId(empresaId);
        avaliacaoLink.setLinkToken(linkToken);
        avaliacaoLink.setExpiraEm(expiracaoEm);
        avaliacaoLinkRepository.save(avaliacaoLink);

        // Return the generated link
        return "https://cuidarmais.com/avaliacao/" + linkToken;



    }


    public Empresa getEmpresa(UUID id) {

        return empresaRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));
    }
}
