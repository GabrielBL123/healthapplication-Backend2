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
    private RespostaGenerator respostaGenerator;

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

        return new RespostaInfoEmpresaDTO(empresa.getNome(), empresa.getCnpj(), nomeSetor);
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
    // ITEM 2: SINALIZAR TÉRMINO (Ação do RH)
    // =========================================================================================
    @Transactional
    public void sinalizarTermino(UUID avaliacaoId) {
        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(avaliacaoId)
                .orElseThrow(() -> new NotFoundException("Avaliação não encontrada"));

        avaliacao.setRhSinalizouTermino(true);
        avaliacaoMensalRepository.save(avaliacao);
    }

    // =========================================================================================
    // LÓGICA DE CÁLCULO DE RISCO (NR-1) ATUALIZADA
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
            String nomeDoSetor = setor.getId() != null ? setor.getId().toString() : "Setor Desconhecido";

            if (totalRespondentes < 3) {
                setoresDTO.add(new RiscoSetorDTO(nomeDoSetor, totalRespondentes, false, 0.0, null, null));
            } else {
                List<RiscoFatorDTO> fatoresSetor = processarFatores(respostasSetor);
                double riscoGeralSetor = calcularMediaRiscos(fatoresSetor);

                setoresDTO.add(new RiscoSetorDTO(
                        nomeDoSetor, totalRespondentes, true, riscoGeralSetor, classificarRisco(riscoGeralSetor), fatoresSetor
                ));
            }
        }

        if (todasRespostasEmpresa.isEmpty()) {
            return new RiscoEmpresaDTO(
                    avaliacao.getEmpresa().getNome(), avaliacao.getEmpresa().getCnpj(),
                    0, 0.0, "Indisponível", new ArrayList<>(), setoresDTO
            );
        }

        List<RiscoFatorDTO> fatoresGlobais = processarFatores(todasRespostasEmpresa);
        double riscoGeralEmpresa = calcularMediaRiscos(fatoresGlobais);

        return new RiscoEmpresaDTO(
                avaliacao.getEmpresa().getNome(), avaliacao.getEmpresa().getCnpj(),
                todasRespostasEmpresa.size(), riscoGeralEmpresa, classificarRisco(riscoGeralEmpresa),
                fatoresGlobais, setoresDTO
        );
    }

    private List<RiscoFatorDTO> processarFatores(List<Resposta> respostas) {
        int totalRespostas = respostas.size();
        double[] somaPerguntas = new double[52];

        for (Resposta r : respostas) {
            List<Integer> valores = r.getValores();
            for (int i = 0; i < 52; i++) {
                somaPerguntas[i] += valores.get(i);
            }
        }

        List<RiscoFatorDTO> fatores = new ArrayList<>();

        for (int i = 0; i < 13; i++) {
            int inicio = i * 4;

            double q1 = somaPerguntas[inicio] / totalRespostas;
            double q2 = somaPerguntas[inicio + 1] / totalRespostas;
            double q3 = somaPerguntas[inicio + 2] / totalRespostas;
            double q4 = somaPerguntas[inicio + 3] / totalRespostas;

            double p = formatarDuasCasas((q1 + q2) / 2.0);
            double c = formatarDuasCasas((q3 + q4) / 2.0);
            double cAjustada = formatarDuasCasas(6.0 - c);

            // NOVA FÓRMULA: Ponderada (P * 0.6) + (C_ajustada * 0.4)
            double r = formatarDuasCasas((p * 0.6) + (cAjustada * 0.4));

            String classificacao = classificarRisco(r);
            String alertaEspecial = null;

            // NOVA REGRA 3: Risco Crítico Direto (P > 4 E C_ajustada < 4)
            if (p > 4.0 && cAjustada < 4.0) {
                classificacao = "Crítico";
                alertaEspecial = "Regra 3: Risco Crítico Direto";
            }
            // Regra 2: Sofrimento Elevado (P >= 4)
            else if (p >= 4.0) {
                if (!classificacao.equals("Crítico")) classificacao = "Alto";
                alertaEspecial = "Regra 2: Sofrimento Elevado";
            }
            // Regra 1: Risco Oculto (P <= 2 E C_ajustada >= 4)
            else if (p <= 2.0 && cAjustada >= 4.0) {
                classificacao = "Alto";
                alertaEspecial = "Regra 1: Risco Oculto";
            }

            fatores.add(new RiscoFatorDTO(i + 1, NOMES_FATORES[i], p, c, cAjustada, r, classificacao, alertaEspecial));
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

    private double formatarDuasCasas(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // =========================================================================================
    // ITEM 3: EXPORTAÇÃO DE ARQUIVO CSV (Ação do Admin)
    // =========================================================================================
    public String gerarRelatorioCsv(UUID avaliacaoId) {
        RiscoEmpresaDTO relatorio = calcularRiscoEmpresa(avaliacaoId);
        StringBuilder csv = new StringBuilder();

        // Cabeçalho das colunas do CSV
        csv.append("Empresa;CNPJ;Setor;Total Respondentes;Risco do Setor;Classificacao do Setor;Fator de Risco;Percepcao;Condicao Ajustada;Risco do Fator;Classificacao do Fator;Alerta Automatico\n");

        for (RiscoSetorDTO setor : relatorio.setores()) {
            if (!setor.exibirResultado()) {
                csv.append(relatorio.nomeEmpresa()).append(";")
                        .append(relatorio.cnpj()).append(";")
                        .append(setor.setorNome()).append(";")
                        .append(setor.totalRespondentes()).append(";")
                        .append("Dados Protegidos (Mínimo de 3 funcionários);;;;;;;\n");
            } else {
                for (RiscoFatorDTO fator : setor.fatores()) {
                    csv.append(relatorio.nomeEmpresa()).append(";")
                            .append(relatorio.cnpj()).append(";")
                            .append(setor.setorNome()).append(";")
                            .append(setor.totalRespondentes()).append(";")
                            .append(setor.riscoGeralSetor()).append(";")
                            .append(setor.classificacaoGeral()).append(";")
                            .append(fator.nomeFator()).append(";")
                            .append(fator.percepcao()).append(";")
                            .append(fator.condicaoAjustada()).append(";")
                            .append(fator.risco()).append(";")
                            .append(fator.classificacao()).append(";")
                            .append(fator.alertaEspecial() != null ? fator.alertaEspecial() : "Nenhum").append("\n");
                }
            }
        }
        return csv.toString();
    }

    public void gerarRespostasAleatorias(int quantidade) {


        /// Cria as entidades primeiro
        Empresa empresa = respostaGenerator.generateRandomEmpresa();

        for (int i = 1; i < 9; i++) {
            respostaGenerator.generateRandomSetor(empresa,i);
        }

        String link = respostaGenerator.generateRandomAvaliacaoMensal(empresa);



        /// Submete as respostas
        for (int i = 1; i < quantidade; i++) {

            RespostaDTO resposta = respostaGenerator.generateRandomResposta();

            submeterResposta(resposta,link);

        }


    }
}