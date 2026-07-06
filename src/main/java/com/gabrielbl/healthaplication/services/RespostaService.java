package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.*;
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

    @Autowired
    private RespostaRepository respostaRepository;

    // --- NOVA CONSTANTE PARA O CÁLCULO DA NR-1 ---
    private static final String[] NOMES_FATORES = {
            "Sobrecarga de Trabalho", "Ritmo Intenso / Pressão", "Liderança",
            "Assédio / Ambiente Tóxico", "Falta de Autonomia", "Falta de Reconhecimento",
            "Comunicação Ineficaz", "Injustiça Organizacional", "Relações Interpessoais",
            "Jornada de Trabalho", "Conflito Trabalho x Vida", "Exigência Emocional",
            "Suporte Organizacional"
    };

    @Transactional
    public void submeterResposta(RespostaDTO data, String token) {

        if(usuarioRepository.findByLogin(data.login()) != null){
            throw new AlreadySubmittedException("A resposta ja foi enviada nesse login");
        }

        AvaliacaoTokenLink tokenLink = tokenLinkRepository.findByToken(token);
        AvaliacaoMensal avaliacao = tokenLink.getAvaliacaoMensal();
        if(avaliacao == null){
            throw new NotFoundException("Avaliacao token nao encontrada");
        }
        if(avaliacao.getIsActive() == false)
            throw new BusinessException("Avaliacao nao iniciada");

        ///  Armazena o Usuario
        Usuario usuario = new Usuario();
        usuario.setNome(data.nome());
        usuario.setRole(UsuarioFuncao.USER);
        usuario.setLogin(data.login());
        usuario.setCargo(data.cargo());
        usuario.setSetor(setorRepository.findByNomeAndEmpresaCnpj(data.setor(), avaliacao.getEmpresa().getCnpj()));
        usuario.setTempoDeTrabalho(data.tempoDeTrabalho());
        usuario.setAvaliacaoMensal(avaliacao);
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
        if(avaliacaoSetor == null)
            throw new NotFoundException("Avaliacao setor nao encontrada");
        resposta.setAvaliacaoSetor(avaliacaoSetor);
        respostaRepository.save(resposta);
    }

    public RespostaInfoEmpresaDTO getRespostaInfoEmpresa(String tokenId) {

        AvaliacaoTokenLink tokenLink = tokenLinkRepository.findByToken(tokenId);
        if(tokenLink == null) throw new NotFoundException("Avaliacao token nao encontrada");

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

        AvaliacaoMensal avaliacaoMensal = avaliacaoMensalRepository.findFirstByEmpresaAndIsActiveOrderByCreatedAtDesc(
                empresa, true).orElseThrow(() -> new NotFoundException("AvaliacaoMensal nao encontrada"));

        Page<Usuario> pageUsuarios = usuarioRepository.findByAvaliacaoMensal(avaliacaoMensal, pageable);

        return pageUsuarios.map(a -> new ListaRespostaDTO(
                a.getNome(), a.getLogin(), a.getCargo(), a.getSetor().getNome(),
                a.getTempoDeTrabalho(), a.getJornada(), LocalDateTime.now()
        ));
    }

    // =========================================================================================
    // NOVOS MÉTODOS: LÓGICA DE CÁLCULO DE RISCO (NR-1)
    // =========================================================================================

    public RiscoEmpresaDTO calcularRiscoEmpresa(UUID avaliacaoId) {
        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(avaliacaoId)
                .orElseThrow(() -> new NotFoundException("Avaliação não encontrada"));

        List<AvaliacaoSetor> setores = avaliacao.getAvaliacaoSetores();
        List<Resposta> todasRespostasEmpresa = new ArrayList<>();
        List<RiscoSetorDTO> setoresDTO = new ArrayList<>();

        for (AvaliacaoSetor setor : setores) {
            List<Resposta> respostasSetor = respostaRepository.findByAvaliacaoSetor(setor);
            todasRespostasEmpresa.addAll(respostasSetor);

            int totalRespondentes = respostasSetor.size();

            // Dica: Ajuste ".getId().toString()" pelo método real que retorna o nome do setor na sua entidade AvaliacaoSetor
            // Ex: setor.getSetor().getNome() ou setor.getSetorNome()
            String nomeDoSetor = setor.getId() != null ? setor.getId().toString() : "Setor Desconhecido";

            // REGRA: Mínimo de 3 funcionários para exibir
            if (totalRespondentes < 3) {
                setoresDTO.add(new RiscoSetorDTO(
                        nomeDoSetor, totalRespondentes, false, 0.0, null, null
                ));
            } else {
                List<RiscoFatorDTO> fatoresSetor = processarFatores(respostasSetor);
                double riscoGeralSetor = calcularMediaRiscos(fatoresSetor);

                setoresDTO.add(new RiscoSetorDTO(
                        nomeDoSetor, totalRespondentes, true, riscoGeralSetor, classificarRisco(riscoGeralSetor), fatoresSetor
                ));
            }
        }

        // Se a empresa inteira tiver menos de 3 respostas
        if (todasRespostasEmpresa.isEmpty()) {
            return new RiscoEmpresaDTO(
                    avaliacao.getEmpresa().getNome(),
                    avaliacao.getEmpresa().getCnpj(),
                    0, 0.0, "Indisponível", new ArrayList<>(), setoresDTO
            );
        }

        // Calcula a visão global da empresa
        List<RiscoFatorDTO> fatoresGlobais = processarFatores(todasRespostasEmpresa);
        double riscoGeralEmpresa = calcularMediaRiscos(fatoresGlobais);

        return new RiscoEmpresaDTO(
                avaliacao.getEmpresa().getNome(),
                avaliacao.getEmpresa().getCnpj(),
                todasRespostasEmpresa.size(),
                riscoGeralEmpresa,
                classificarRisco(riscoGeralEmpresa),
                fatoresGlobais,
                setoresDTO
        );
    }

    private List<RiscoFatorDTO> processarFatores(List<Resposta> respostas) {
        int totalRespostas = respostas.size();
        double[] somaPerguntas = new double[52];

        // Soma todas as respostas (índice a índice)
        for (Resposta r : respostas) {
            List<Integer> valores = r.getValores();
            for (int i = 0; i < 52; i++) {
                somaPerguntas[i] += valores.get(i);
            }
        }

        List<RiscoFatorDTO> fatores = new ArrayList<>();

        for (int i = 0; i < 13; i++) {
            int inicio = i * 4;

            // Calcula a média de cada pergunta (dividindo pelo total de respondentes)
            double q1 = somaPerguntas[inicio] / totalRespostas;
            double q2 = somaPerguntas[inicio + 1] / totalRespostas;
            double q3 = somaPerguntas[inicio + 2] / totalRespostas;
            double q4 = somaPerguntas[inicio + 3] / totalRespostas;

            // Fórmulas base (P e C)
            double p = formatarDuasCasas((q1 + q2) / 2.0);
            double c = formatarDuasCasas((q3 + q4) / 2.0);

            // Inversão da condição real
            double cAjustada = formatarDuasCasas(6.0 - c);

            // Cálculo do risco bruto
            double r = formatarDuasCasas((p + cAjustada) / 2.0);

            String classificacao = classificarRisco(r);
            String alertaEspecial = null;

            // Aplicando regras de ouro do sistema em cascata (da mais crítica para a menor)
            if (p >= 4.0 && cAjustada >= 4.0) {
                classificacao = "Crítico";
                alertaEspecial = "Regra 3: Risco Crítico Direto (Percepção e Condição Críticas)";
            } else if (p >= 4.0) {
                if (!classificacao.equals("Crítico")) classificacao = "Alto";
                alertaEspecial = "Regra 2: Sofrimento Elevado (A percepção individual exige atenção alta)";
            } else if (p <= 2.0 && cAjustada >= 4.0) {
                classificacao = "Alto";
                alertaEspecial = "Regra 1: Risco Oculto (Funcionários não percebem, mas a condição real está ruim)";
            }

            fatores.add(new RiscoFatorDTO(
                    i + 1, NOMES_FATORES[i], p, c, cAjustada, r, classificacao, alertaEspecial
            ));
        }

        return fatores;
    }

    private double calcularMediaRiscos(List<RiscoFatorDTO> fatores) {
        double soma = fatores.stream().mapToDouble(RiscoFatorDTO::risco).sum();
        return formatarDuasCasas(soma / fatores.size());
    }

    private String classificarRisco(double score) {
        if (score < 2.0) return "Baixo";
        if (score < 3.0) return "Médio";
        if (score < 4.0) return "Alto";
        return "Crítico";
    }

    // Utilitário para evitar dízimas periódicas longas
    private double formatarDuasCasas(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}