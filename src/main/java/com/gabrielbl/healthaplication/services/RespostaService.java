package com.gabrielbl.healthaplication.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

        if(empresaRepository.findByCnpj("012345678901234")!=null) throw new BusinessException("Avaliacao teste ja realizada.");
        /// Cria as entidades primeiro
        respostaGenerator.generateRandomEmpresa();

        for (int i = 0; i < 6; i++) {
            respostaGenerator.generateRandomSetor(i);
        }

        String link = respostaGenerator.generateRandomAvaliacaoMensal();

        /// Submete as respostas
        for (int i = 0; i < quantidade; i++) {
            RespostaDTO resposta = respostaGenerator.generateRandomResposta();
            submeterResposta(resposta,link);
        }
    }

    // =========================================================================================
    // EXPORTAÇÃO DE ARQUIVO EXCEL FORMATADO (Múltiplas Abas)
    // =========================================================================================
    public byte[] gerarRelatorioExcel(UUID avaliacaoId) {
        // 1. Calcula os riscos de todos os setores usando a sua lógica
        RiscoEmpresaDTO relatorio = calcularRiscoEmpresa(avaliacaoId);

        // 2. Abre o template original
        try (InputStream is = getClass().getResourceAsStream("/template_dashboard.xlsx");
             Workbook workbook = new XSSFWorkbook(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (is == null) {
                throw new RuntimeException("Template do Excel não encontrado!");
            }

            // Pega a primeira aba para usar como molde
            Sheet abaMolde = workbook.getSheetAt(0);

            int indexAba = 0;
            for (RiscoSetorDTO setor : relatorio.setores()) {
                Sheet abaSetor;

                // Se for o primeiro setor, usa a aba que já existe. Se forem os seguintes, clona o visual do molde!
                if (indexAba == 0) {
                    abaSetor = abaMolde;
                    workbook.setSheetName(0, setor.setorNome());
                } else {
                    abaSetor = workbook.cloneSheet(0);
                    workbook.setSheetName(workbook.getSheetIndex(abaSetor), setor.setorNome());
                }
                indexAba++;

                // --- PREENCHIMENTO DOS DADOS DO SETOR NA ABA CORRESPONDENTE ---

                // CNPJ (Linha 4, Coluna B -> Row index 3, Cell index 1)
                Row linhaCnpj = abaSetor.getRow(3);
                if (linhaCnpj == null) linhaCnpj = abaSetor.createRow(3);
                linhaCnpj.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(relatorio.cnpj());

                // Nome do Setor (Linha 5, Coluna B -> Row index 4, Cell index 1)
                Row linhaSetor = abaSetor.getRow(4);
                if (linhaSetor == null) linhaSetor = abaSetor.createRow(4);
                linhaSetor.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(setor.setorNome());

                // Quantidade de Trabalhadores (Linha 7, Coluna B -> Row index 6, Cell index 1)
                Row linhaTrab = abaSetor.getRow(6);
                if (linhaTrab == null) linhaTrab = abaSetor.createRow(6);
                linhaTrab.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(setor.totalRespondentes());

                // Se não atingir o limite mínimo de 3 respondentes, protege as informações confidenciais
                if (!setor.exibirResultado()) {
                    Row rowAviso = abaSetor.getRow(12);
                    if (rowAviso == null) rowAviso = abaSetor.createRow(12);
                    rowAviso.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .setCellValue("DADOS PROTEGIDOS (Mínimo de 3 respondentes no setor para exibir os resultados)");
                    continue; // Pula para o próximo setor
                }

                // Se atingir 3 ou mais respostas, preenche a tabela de riscos (Começa na linha 13 -> Row index 12)
                int linhaAtual = 12;
                for (RiscoFatorDTO fator : setor.fatores()) {
                    Row row = abaSetor.getRow(linhaAtual);
                    if (row == null) row = abaSetor.createRow(linhaAtual);

                    // Coluna C (índice 2): Gravidade (Condição Ajustada)
                    row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(fator.condicaoAjustada());

                    // Coluna D (índice 3): Probabilidade (Percepção)
                    row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(fator.percepcao());

                    // Coluna E (índice 4): Matriz de Risco (Resultado classificado)
                    row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(fator.classificacao());

                    linhaAtual++;
                }
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o Excel formatado: " + e.getMessage());
        }
    }
}