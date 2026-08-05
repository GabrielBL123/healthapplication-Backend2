package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.*;
import com.gabrielbl.healthaplication.repository.*;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Importação necessária para buscar o usuário logado
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private AutorizacaoService autorizacaoService;

    public void criarEIniciarAvaliacaoMensal(String cnpj) {

        Empresa empresa = empresaRepository.findByCnpj(cnpj);

        autorizacaoService.verificarAcessoEmpresa(empresa);

        if(empresa==null) throw new NotFoundException("Empresa nao encontrada");

        if(empresa.getSetores().isEmpty()) throw new NotFoundException("Empresa nao possui setores");

        if((avaliacaoMensalRepository.findByEmpresaIdAndIsActive(
                empresa.getId(),true)!=null)){
            throw new AlreadySubmittedException("Avaliacao Mensal ja ativa nessa empresa");
        }

        AvaliacaoMensal avaliacaoMensal = new AvaliacaoMensal();
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

    public void finalizarAvaliacaoMensal(String cnpj) {

        Empresa empresa = empresaRepository.findByCnpj(cnpj);
        if(empresa ==null) throw new NotFoundException("Empresa nao encontrada");

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findByEmpresaIdAndIsActive(empresa.getId(),true);
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

    // --- BLOQUEIO DE SEGURANÇA NA LISTAGEM GERAL ---
    public Page<AvaliacaoMensalResponseDTO> getAll(Pageable pageable) {
        // Captura o usuário autenticado na requisição atual
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<AvaliacaoMensal> page;

        // Verifica se é RH. (Ajuste o getRole().name() conforme o nome exato do seu Enum ou método na classe Usuario)
        if (usuarioLogado.getRole().name().equals("RH") || usuarioLogado.getRole().name().equals("ROLE_RH")) {
            // Se for RH, força o banco a trazer SÓ as avaliações da empresa dele
            page = avaliacaoMensalRepository.findByEmpresa(usuarioLogado.getEmpresa(), pageable);
        } else {
            // Se for Admin, traz tudo normalmente
            page = avaliacaoMensalRepository.findAll(pageable);
        }

        return page.map(a ->
                new AvaliacaoMensalResponseDTO(a.getId().toString(),
                        a.getCreatedAt().toString().replace("T", " "),
                        a.getIsActive(),
                        a.getEmpresa().getCnpj()));
    }

    public Page<AvaliacaoMensalResponseDTO> getEmpresaAvaliacoes(Pageable pageable,UUID empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));

        autorizacaoService.verificarAcessoEmpresa(empresa);


        Page<AvaliacaoMensal> page =  avaliacaoMensalRepository.findByEmpresa(empresa,pageable);

        return page.map(a ->
                new AvaliacaoMensalResponseDTO(a.getId().toString(),
                        a.getCreatedAt().toString().replace("T", " "),
                        a.getIsActive(),
                        a.getEmpresa().getCnpj()));
    }

    public String getLinkAvaliacao(String cnpj) {
        // 1. Buscar empresa
        Empresa empresa = empresaRepository.findByCnpj(cnpj);
        if(empresa == null) {
            throw new NotFoundException("Empresa não encontrada");
        }

        //verifica se possui acesso à empresa
        autorizacaoService.verificarAcessoEmpresa(empresa);

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

        return linkToken;
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



    // --- BLOQUEIO DE SEGURANÇA NOS DETALHES ---
    public AvaliacaoMensalComSetoresResponseDTO getAvaliacao(String avaliacaoId) {

        //pega a avaliacao
        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(UUID.fromString(avaliacaoId))
                .orElseThrow(() -> new NotFoundException("Avaliacao nao encontrada"));
        //verifica se possui acesso
        autorizacaoService.verificarAcessoEmpresa(avaliacao.getEmpresa());


        //organiza os dados para o retorno
        List<Usuario> funcionarios = avaliacao.getUsuarios();

        List<FuncionarioDTO> funcionariosDTO = funcionarios.stream()
                .map(a -> new FuncionarioDTO(
                        a.getLogin(), a.getNome(), a.getSetor().getNome(), a.getCargo(),
                        a.getTempoDeTrabalho(), a.getJornada()
                ))
                .toList();

        Empresa empresa = avaliacao.getEmpresa();

        EmpresaResponseDTO empresaResponseDTO = new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getCnpj(),
                empresa.getNome(),
                empresa.getEmail(),
                empresa.getTelefone(),
                avaliacao.getAvaliacaoSetores().stream()
                        .map(a ->
                                new SetorResponseDTO(
                                        a.getId(), a.getSetor().getNome(),
                                        a.getAvaliacaoMensal().getEmpresa().getId(),
                                        a.getAvaliacaoMensal().getEmpresa().getNome())
                        )
                        .toList()
        );

        return new AvaliacaoMensalComSetoresResponseDTO(
                avaliacaoId,
                avaliacao.getCreatedAt(),
                avaliacao.getIsActive(),
                empresaResponseDTO,
                funcionariosDTO
        );
    }


}