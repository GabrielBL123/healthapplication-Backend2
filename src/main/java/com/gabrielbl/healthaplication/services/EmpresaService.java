package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.EmpresaResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.AvaliacaoTokenLink;
import com.gabrielbl.healthaplication.repository.AvaliacaoMensalRepository;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.AvaliacaoTokenLinkRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.UUID;


@Service

public class EmpresaService {



    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AvaliacaoMensalRepository avaliacaoMensalRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository avaliacaoTokenLinkRepository;

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


    public Page<EmpresaResponseDTO> getAllEmpresas(Pageable pageable) {

        Page<Empresa> page = empresaRepository.findAll(pageable);

        return page.map(a ->
                new EmpresaResponseDTO(a.getId(),a.getCnpj(),a.getNome(),a.getEmail(),a.getTelefone()));
    }


    /*
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
        AvaliacaoTokenLink avaliacaoTokenLink = new AvaliacaoTokenLink();
        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(empresaId,true);
        avaliacaoTokenLink.setAvaliacaoMensal(avaliacao);
        avaliacaoTokenLink.setToken(linkToken);
        avaliacaoTokenLink.setDataExpiracao(expiracaoEm);
        avaliacaoTokenLinkRepository.save(avaliacaoTokenLink);

        // Return the generated link
        return "https://cuidarmais.com/avaliacao/" + linkToken;



    }


     */

    public Empresa getEmpresa(UUID id) {

        return empresaRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));
    }
}
