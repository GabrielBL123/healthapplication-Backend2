package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.*;
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
import java.util.Optional;
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
        avaliacaoMensal.setCreatedAt(LocalDateTime.now());

        List<AvaliacaoSetor> setores = new ArrayList<>();
        for (Setor Setor : empresa.getSetores()){
            AvaliacaoSetor avaliacaoSetor = new AvaliacaoSetor();
            avaliacaoSetor.setSetor(Setor);
            avaliacaoSetor.setAvaliacaoMensal(avaliacaoMensal);
            avaliacaoSetorRepository.save(avaliacaoSetor);
            setores.add(avaliacaoSetor);
        }

        avaliacaoMensal.setAvaliacaoSetores(setores);


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

    public void deletarAvaliacaoMensal(String id) {

        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Avaliacao nao encontrada"));

        Empresa empresa =  empresaRepository.findByCnpj(avaliacao.getEmpresa().getCnpj());
        if(empresa==null) throw new NotFoundException("Empresa nao encontrada");


        avaliacaoMensalRepository.delete(avaliacao);

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

    public String getLinkAvaliacao(String cnpj) {
        // 1. Buscar empresa
        Empresa empresa = empresaRepository.findByCnpj(cnpj);
        if(empresa == null) {
            throw new NotFoundException("Empresa não encontrada");
        }

        // 2. Buscar avaliação ativa
        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository
                .findFirstByEmpresaAndIsActiveOrderByCreatedAtDesc(empresa, true)
                .orElseThrow(() -> new NotFoundException(
                        "Nenhuma avaliação mensal ativa encontrada para esta empresa"
                ));

        // 3. Buscar token válido existente
        Optional<AvaliacaoTokenLink> tokenValido = avaliacaoTokenLinkRepository
                .findFirstByAvaliacaoMensalAndIsActiveOrderByExpiracaoEmDesc(
                        avaliacaoMensal,
                        true
                )
                .filter(AvaliacaoTokenLink::isValid);

        String linkToken;

        if(tokenValido.isPresent()) {
            // Token válido existe, reutilizar
            linkToken = tokenValido.get().getToken();
        } else {
            // Token expirou ou não existe, criar novo
            linkToken = gerarNovoToken(avaliacaoMensal);
        }

        return "https://cuidarmais.com/avaliacao/" + linkToken;
    }


    private String gerarNovoToken(AvaliacaoMensal avaliacaoMensal) {
        // 1. Desativar todos os tokens antigos (opcional, mas recomendado)
        avaliacaoTokenLinkRepository.desativarTodosOsTokens(avaliacaoMensal.getId());

        // 2. Criar novo token
        String novoToken = UUID.randomUUID().toString();

        AvaliacaoTokenLink novoLink = new AvaliacaoTokenLink();
        novoLink.setToken(novoToken);
        novoLink.setAvaliacaoMensal(avaliacaoMensal);
        novoLink.setExpiracaoEm(LocalDateTime.now().plusDays(5));
        novoLink.setIsActive(true);

        avaliacaoTokenLinkRepository.save(novoLink);

        return novoToken;
    }


    public AvaliacaoTokenLink validarToken(String token) {
        return avaliacaoTokenLinkRepository
                .findByTokenAndIsActive(token, true)
                .filter(AvaliacaoTokenLink::isValid)
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));
    }




    private void competenciaVencida(String competencia) {

        if (competencia == null || !competencia.matches("\\d{8}")) {
            throw new IllegalArgumentException("competencia deve ser no formato YYYYMMDD");
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate dataCompetencia = LocalDate.parse(competencia, formatter);

        if (dataCompetencia.isAfter(LocalDate.now())) throw new  IllegalArgumentException("data invalida");
    }

    public AvaliacaoMensalComSetoresResponseDTO getAvaliacao(String avaliacaoId) {

        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(UUID.fromString(avaliacaoId))
                .orElseThrow(() -> new NotFoundException("Avaliacao nao encontrada"));

        List<Usuario> funcionarios = avaliacao.getUsuarios();

        List<FuncionarioDTO> funcionariosDTO = funcionarios.stream()
                .map(a -> new FuncionarioDTO(
                    a.getLogin(), a.getNome(), a.getSetor().toString(), a.getCargo(),
                    a.getTempoDeTrabalho(), a.getJornada()
                ))
                .toList();

        Empresa empresa = avaliacao.getEmpresa();

        EmpresaResponseDTO empresaResponseDTO = new EmpresaResponseDTO(
                empresa.getId(),empresa.getCnpj(), empresa.getNome(),
                empresa.getEmail(), empresa.getTelefone(),
                empresa.getSetores().stream()
                        .map(a -> new SetorResponseDTO(a.getId(),a.getNome(),
                                a.getEmpresa().getId(),a.getEmpresa().getNome()))
                        .toList()
        );






        return new AvaliacaoMensalComSetoresResponseDTO(
                avaliacaoId,avaliacao.getCompetencia(),avaliacao.getCreatedAt(),
                avaliacao.getIsActive(), empresaResponseDTO,funcionariosDTO
        );

    }
}
