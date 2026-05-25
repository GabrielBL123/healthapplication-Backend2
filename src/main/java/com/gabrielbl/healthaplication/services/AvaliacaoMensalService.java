package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalDTO;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.GerarLinkDTO;
import com.gabrielbl.healthaplication.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AvaliacaoMensalService {

    @Autowired
    private AvaliacaoMensalRepository avaliacaoMensalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AvaliacaoSetorRepository avaliacaoSetorRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository avaliacaoTokenLinkRepository;

    public void criarEIniciarAvaliacaoMensal(AvaliacaoMensalDTO data) {



        competenciaVencida(data.competencia());

        Empresa empresa = empresaRepository.findByCnpj(data.cnpj());


        if(empresa==null){
            throw new NotFoundException("Empresa nao encontrada");
        }

        if((avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(
                data.competencia(), empresa.getId(),true)!=null)){
            throw new AlreadySubmittedException("Avaliacao Mensal ja ativa nessa empresa");
        }




        AvaliacaoMensal avaliacaoMensal = new AvaliacaoMensal();
        avaliacaoMensal.setCompetencia(data.competencia());
        avaliacaoMensal.setIsActive(true);
        avaliacaoMensal.setEmpresa(empresa);


        List<AvaliacaoSetor> setores = new ArrayList<>();
        for (Setor Setor : empresa.getSetores()){
            AvaliacaoSetor avaliacaoSetor = new AvaliacaoSetor();
            avaliacaoSetor.setSetor(Setor);
            avaliacaoSetor.setAvaliacaoMensal(avaliacaoMensal);
            avaliacaoSetorRepository.save(avaliacaoSetor);
            setores.add(avaliacaoSetor);
        }

        avaliacaoMensal.setAvaliacaoSetores(setores);
        avaliacaoMensal.setCreatedAt(LocalDateTime.now());

        avaliacaoMensalRepository.save(avaliacaoMensal);



    }


    public void finalizarAvaliacaoMensal(AvaliacaoMensalDTO data) {


        Empresa empresa = empresaRepository.findByCnpj(data.cnpj());
        if(empresa ==null) throw new NotFoundException("Empresa nao encontrada");

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(data.competencia(), empresa.getId(),true);
        if(avaliacaoMensal.getIsActive()==false) throw new NotFoundException("Avaliacao Mensal ativa nao existente");

        avaliacaoMensal.setIsActive(false);
        avaliacaoMensal.setSubmittedAt(LocalDateTime.now());



    }

    public void deletarAvaliacaoMensal(AvaliacaoMensalDTO data) {

        Empresa empresa = empresaRepository.findByCnpj(data.cnpj());
        if(empresa ==null) throw new NotFoundException("Empresa nao encontrada");

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByCompetenciaAndEmpresaId(data.competencia(), empresa.getId());
        if(avaliacaoMensal ==null) throw new NotFoundException("Avaliacao Mensal nao encontrada");

        avaliacaoMensalRepository.delete(avaliacaoMensal);

    }


    public Page<AvaliacaoMensalResponseDTO> getAll(Pageable pageable) {


        Page<AvaliacaoMensal> page = avaliacaoMensalRepository.findAll(pageable);

        return page.map(a ->
                        new AvaliacaoMensalResponseDTO(a.getId().toString(),a.getCompetencia(),a.getIsActive()));
    }

    public Page<AvaliacaoMensalResponseDTO> getEmpresaAvaliacoes(Pageable pageable,UUID empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));

        Page<AvaliacaoMensal> page =  avaliacaoMensalRepository.findByEmpresa(empresa,pageable);

        return page.map(a ->
                new AvaliacaoMensalResponseDTO(a.getId().toString(),a.getCompetencia(),a.getIsActive()));
    }

    public String gerarLinkAvaliacao(String cnpj, Integer horasExpiracao) {
        Empresa empresa = empresaRepository.findByCnpj(cnpj);
        if(empresa ==null) throw new NotFoundException("Empresa nao encontrada");

        // Generate a unique link token
        String linkToken = UUID.randomUUID().toString();

        // Calculate expiry time (optional)
        LocalDateTime expiracaoEm = horasExpiracao != null
                ? LocalDateTime.now().plusHours(horasExpiracao)
                : null;

        // Save the link information in the database
        AvaliacaoTokenLink avaliacaoLink = new AvaliacaoTokenLink();

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByEmpresaAndIsActive(empresa, true);

        avaliacaoLink.setAvaliacaoMensal(avaliacaoMensal);
        avaliacaoLink.setToken(linkToken);
        avaliacaoLink.setDataExpiracao(expiracaoEm);
        avaliacaoTokenLinkRepository.save(avaliacaoLink);

        // Return the generated link
        return "https://cuidarmais.com/avaliacao/" + linkToken;



    }




    private void competenciaVencida(String competencia) {

        if (competencia == null || !competencia.matches("\\d{8}")) {
            throw new IllegalArgumentException("competencia deve ser no formato YYYYMMDD");
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate dataCompetencia = LocalDate.parse(competencia, formatter);

        if (dataCompetencia.isAfter(LocalDate.now())) throw new  IllegalArgumentException("data invalida");
    }
}
