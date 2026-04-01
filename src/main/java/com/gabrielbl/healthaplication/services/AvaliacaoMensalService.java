package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalIniciarDTO;
import com.gabrielbl.healthaplication.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void criarEIniciarAvaliacaoMensal(AvaliacaoMensalIniciarDTO data,String emailUsuario) {


        Usuario usuario = usuarioRepository.findByLogin(emailUsuario);
        Empresa empresa = usuario.getEmpresa();

        if((avaliacaoMensalRepository.findByCompetenciaAndEmpresaIdAndIsActive(
                data.competencia(), empresa.getId(),true)!=null)){
            throw new AlreadySubmittedException("Avaliacao Mensal ja existente e ativa nessa empresa");
        }




        AvaliacaoMensal avaliacaoMensal = new AvaliacaoMensal();
        avaliacaoMensal.setCompetencia(data.competencia());
        avaliacaoMensal.setIsActive(true);
        avaliacaoMensal.setEmpresa(empresa);


        List<AvaliacaoSetor> setores = new ArrayList<>();
        for (Setor Setor : empresa.getSetor()){
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


    public void finalizarAvaliacaoMensal(String competencia,String emailUsuario) {

        if((avaliacaoMensalRepository.findByCompetenciaAndIsActive(competencia,false)!=null)){


        }


    }



}
