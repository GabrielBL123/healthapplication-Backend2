package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.GerarLinkDTO;
import com.gabrielbl.healthaplication.model.DTOs.ListaRespostaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaInfoEmpresaDTO;
import com.gabrielbl.healthaplication.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RespostaService {

    @Autowired
    private AvaliacaoMensalRepository avaliacaoMensalRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository tokenLinkRepository;

    @Autowired
    private AvaliacaoSetorRepository avaliacaoSetorRepository;


    @Transactional
    public void submeterResposta(RespostaDTO data,String token) {


        if(usuarioRepository.findByLogin(data.login())!=null){
            throw new AlreadySubmittedException("A resposta ja foi enviada nesse login");
        }

        AvaliacaoTokenLink tokenLink = tokenLinkRepository.findByToken(token);
        AvaliacaoMensal avaliacao = tokenLink.getAvaliacaoMensal();
        if(avaliacao==null){
            throw  new NotFoundException("Avaliacao token nao encontrada");
        }
        if(avaliacao.getIsActive()==false)
            throw new BusinessException("Avaliacao nao iniciada");

        ///  Armazena o Usuario
        Usuario usuario = new Usuario();
        usuario.setNome(data.nome());
        usuario.setRole(UsuarioFuncao.USER);
        usuario.setLogin(data.login());
        usuario.setCargo(data.cargo());
        usuario.setSetor(setorRepository.findByNomeAndEmpresaCnpj(data.setor(), avaliacao.getEmpresa().getCnpj()));
        usuario.setTempoDeTrabalho(data.tempoDeTrabalho());
        usuario.setJornada(data.jornada());
        usuario.setEmpresa(avaliacao.getEmpresa());
        usuarioRepository.save(usuario);

        /// Armazena a Resposta
        Resposta resposta = new Resposta();
        resposta.setUsuario(usuario);
        List<Integer> valores = new ArrayList<>();
        for (int valor : data.resposta())
            valores.add(valor);
        resposta.setValores(valores);
        resposta.setToken(tokenLink);
        resposta.setCreatedAt(LocalDateTime.now());
        AvaliacaoSetor avaliacaoSetor = avaliacaoSetorRepository.findBySetorNomeAndAvaliacaoMensal(data.setor(), avaliacao);
        if(avaliacaoSetor==null)
            throw new NotFoundException("Avaliacao setor nao encontrada");
        resposta.setAvaliacaoSetor(avaliacaoSetor);



    }

    public RespostaInfoEmpresaDTO getRespostaInfoEmpresa(String tokenId) {

        AvaliacaoTokenLink tokenLink = tokenLinkRepository.findByToken(tokenId);
        AvaliacaoMensal avaliacao = tokenLink.getAvaliacaoMensal();
        Empresa empresa = avaliacao.getEmpresa();

        List<String> nomeSetor = new ArrayList<>();

        for (Setor setor : empresa.getSetores())
            nomeSetor.add(setor.getNome());


        return new RespostaInfoEmpresaDTO(
                empresa.getNome(),
                empresa.getCnpj(),
                nomeSetor
        );
    }

    public Page<ListaRespostaDTO> getAllRespostaInfo(String empresaId, Pageable pageable) {

        Empresa empresa = empresaRepository.findById(UUID.fromString(empresaId)).orElseThrow(
                () -> new NotFoundException("Empresa nao encontrada")
        );

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByEmpresaAndIsActive(
                empresa,true);
        if(avaliacaoMensal==null)
            throw new NotFoundException("Avaliacao mensal nao encontrada ou nao ativada");

        Page<Usuario> pageUsuarios = usuarioRepository.findByAvaliacaoMensal(avaliacaoMensal, pageable);

        return pageUsuarios.map(a -> new ListaRespostaDTO(
                a.getNome(),a.getLogin(),a.getCargo(),a.getSetor().getNome(),
                a.getTempoDeTrabalho(),a.getJornada(),LocalDateTime.now()
        ));

    }
}
