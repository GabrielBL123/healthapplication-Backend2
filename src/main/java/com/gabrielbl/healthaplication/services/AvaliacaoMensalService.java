package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalDTO;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalResponseDTO;
import com.gabrielbl.healthaplication.repository.*;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public void criarEIniciarAvaliacaoMensal(AvaliacaoMensalDTO data) {



        Empresa empresa = empresaRepository.findByCnpj(data.cnpj());

        if((avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(
                data.competencia(), empresa.getId(),true)!=null)){
            throw new AlreadySubmittedException("Avaliacao Mensal ja existente e ativa nessa empresa");
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
                //avaliacaoSetorRepository.save(avaliacaoSetor);
            setores.add(avaliacaoSetor);
        }

        avaliacaoMensal.setAvaliacaoSetores(setores);
        avaliacaoMensal.setCreatedAt(LocalDateTime.now());

        avaliacaoMensalRepository.save(avaliacaoMensal);



    }


    public void finalizarAvaliacaoMensal(AvaliacaoMensalDTO data) {


        Empresa empresa = empresaRepository.findByCnpj(data.cnpj());
        if(empresa ==null) throw new NotFoundException("Empresa nao encontrada");

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(data.competencia(), empresa.getId(),true   );
        if(avaliacaoMensal.getIsActive()==false) throw new NotFoundException("Avaliacao Mensal ativa nao existente");

        avaliacaoMensal.setIsActive(false);




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
                        new AvaliacaoMensalResponseDTO(a.getId(),a.getCompetencia(),a.getIsActive()));
    }

    public Page<AvaliacaoMensalResponseDTO> getEmpresaAvaliacoes(Pageable pageable,UUID empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));

        Page<AvaliacaoMensal> page =  avaliacaoMensalRepository.findByEmpresa(empresa,pageable);

        return page.map(a ->
                new AvaliacaoMensalResponseDTO(a.getId(),a.getCompetencia(),a.getIsActive()));
    }
}
