package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.GerarLinkDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.repository.AvaliacaoMensalRepository;
import com.gabrielbl.healthaplication.repository.AvaliacaoTokenLinkRepository;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RespostaService {

    @Autowired
    private AvaliacaoMensalRepository avaliacaoMensalRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository avaliacaoTokenLinkRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository tokenLinkRepository;

    public void submeterResposta(RespostaDTO data,String token) {


        if(usuarioRepository.findByLogin(data.login())==null){
            throw new AlreadySubmittedException("A resposta ja foi enviada nesse login");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(data.nome());
        usuario.setRole(UsuarioFuncao.USER);
        usuario.setLogin(data.login());
        usuario.setTempoDeTrabalho(data.tempoDeTrabalho());
        usuario.setJornada(data.jornada());

        AvaliacaoTokenLink tokenLink = tokenLinkRepository.findByToken(token);
        AvaliacaoMensal avaliacao = tokenLink.getAvaliacaoMensal();
        usuario.setEmpresa(avaliacao.getEmpresa());

        usuarioRepository.save(usuario);
    }
}
