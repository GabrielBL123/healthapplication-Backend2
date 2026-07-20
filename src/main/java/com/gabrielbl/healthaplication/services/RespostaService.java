package com.gabrielbl.healthaplication.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

    @Transactional
    public void sinalizarTermino(UUID avaliacaoId) {
        AvaliacaoMensal avaliacao = avaliacaoMensalRepository.findById(avaliacaoId)
                .orElseThrow(() -> new NotFoundException("Avaliação não encontrada"));

        avaliacao.setRhSinalizouTermino(true);
        avaliacaoMensalRepository.save(avaliacao);
    }

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
            String nomeDoSetor = setor.getSetor() != null ? setor.getSetor().getNome() : "Setor Desconhecido";

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

            double r = formatarDuasCasas((p * 0.6) + (cAjustada * 0.4));

            String classificacao = classificarRisco(r);
            String alertaEspecial = null;

            if (p > 4.0 && cAjustada < 4.0) {
                classificacao = "Crítico";
                alertaEspecial = "Regra 3: Risco Crítico Direto";
            } else if (p >= 4.0) {
                if (!classificacao.equals("Crítico")) classificacao = "Alto";
                alertaEspecial = "Regra 2: Sofrimento Elevado";
            } else if (p <= 2.0 && cAjustada >= 4.0) {
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

    public String gerarRelatorioCsv(UUID avaliacaoId) {
        RiscoEmpresaDTO relatorio = calcularRiscoEmpresa(avaliacaoId);
        StringBuilder csv = new StringBuilder();

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

        respostaGenerator.generateRandomEmpresa();

        for (int i = 0; i < 6; i++) {
            respostaGenerator.generateRandomSetor(i);
        }

        String link = respostaGenerator.generateRandomAvaliacaoMensal();

        for (int i = 0; i < quantidade; i++) {
            RespostaDTO resposta = respostaGenerator.generateRandomResposta();
            submeterResposta(resposta,link);
        }
    }

    public byte[] gerarRelatorioExcel(UUID avaliacaoId) {
        RiscoEmpresaDTO relatorio = calcularRiscoEmpresa(avaliacaoId);

        try (InputStream is = getClass().getResourceAsStream("/template_dashboard.xlsx");
             Workbook workbook = new XSSFWorkbook(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (is == null) {
                throw new RuntimeException("Template do Excel não encontrado!");
            }

            Sheet abaMolde = workbook.getSheetAt(0);

            int indexAba = 0;
            for (RiscoSetorDTO setor : relatorio.setores()) {
                Sheet abaSetor;

                if (indexAba == 0) {
                    abaSetor = abaMolde;
                    workbook.setSheetName(0, setor.setorNome());
                } else {
                    abaSetor = workbook.cloneSheet(0);
                    workbook.setSheetName(workbook.getSheetIndex(abaSetor), setor.setorNome());
                }
                indexAba++;

                // --- INÍCIO: INJEÇÃO AUTOMÁTICA DA FORMATAÇÃO CONDICIONAL VIA JAVA ---
                SheetConditionalFormatting sheetCF = abaSetor.getSheetConditionalFormatting();

                // Regra Crítico (Vermelho, texto Preto)
                ConditionalFormattingRule regraCritico = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"Crítico\"");
                PatternFormatting fundoCritico = regraCritico.createPatternFormatting();
                fundoCritico.setFillBackgroundColor(IndexedColors.RED.index);
                fundoCritico.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
                regraCritico.createFontFormatting().setFontColorIndex(IndexedColors.BLACK.index);

                // Regra Alto (Laranja, texto Preto)
                ConditionalFormattingRule regraAlto = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"Alto\"");
                PatternFormatting fundoAlto = regraAlto.createPatternFormatting();
                fundoAlto.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.index);
                fundoAlto.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
                regraAlto.createFontFormatting().setFontColorIndex(IndexedColors.BLACK.index);

                // Regra Médio (Amarelo, texto Preto)
                ConditionalFormattingRule regraMedio = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"Médio\"");
                PatternFormatting fundoMedio = regraMedio.createPatternFormatting();
                fundoMedio.setFillBackgroundColor(IndexedColors.YELLOW.index);
                fundoMedio.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
                regraMedio.createFontFormatting().setFontColorIndex(IndexedColors.BLACK.index);

                // Regra Baixo (Verde, texto Preto)
                ConditionalFormattingRule regraBaixo = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"Baixo\"");
                PatternFormatting fundoBaixo = regraBaixo.createPatternFormatting();
                fundoBaixo.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.index);
                fundoBaixo.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
                regraBaixo.createFontFormatting().setFontColorIndex(IndexedColors.BLACK.index);

                // Aplica nas colunas C, D e E (índices 2, 3 e 4) da linha 12 até a 30 (índices 11 a 29)
                CellRangeAddress[] regioes = { new CellRangeAddress(11, 29, 2, 4) };

                sheetCF.addConditionalFormatting(regioes, regraCritico);
                sheetCF.addConditionalFormatting(regioes, regraAlto);
                sheetCF.addConditionalFormatting(regioes, regraMedio);
                sheetCF.addConditionalFormatting(regioes, regraBaixo);
                // --- FIM DA FORMATAÇÃO CONDICIONAL ---

                // --- PREENCHIMENTO DOS DADOS DO SETOR NA ABA CORRESPONDENTE ---

                // 1. CNPJ e Data de Elaboração (Estão na mesma Linha 3 do Excel -> índice 2)
                Row linhaCnpj = abaSetor.getRow(2);
                if (linhaCnpj == null) linhaCnpj = abaSetor.createRow(2);

                // Injeta o CNPJ na Coluna B (Índice 1)
                linhaCnpj.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(relatorio.cnpj());

                // Cria a data atual formatada (dd/MM/yyyy) e injeta na Coluna D (Índice 3)
                String dataAtual = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                linhaCnpj.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(dataAtual);

                // 2. Nome do Setor (Linha 4 no Excel -> índice 3, Coluna B -> índice 1)
                Row linhaSetor = abaSetor.getRow(3);
                if (linhaSetor == null) linhaSetor = abaSetor.createRow(3);
                linhaSetor.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(setor.setorNome());

                // 3. Quantidade de Trabalhadores (Linha 6 no Excel -> índice 5, Coluna B -> índice 1)
                Row linhaTrab = abaSetor.getRow(5);
                if (linhaTrab == null) linhaTrab = abaSetor.createRow(5);
                linhaTrab.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(setor.totalRespondentes());

                // Se não atingir o limite mínimo, protege os dados mas MANTÉM a tabela
                // Se não atingir o limite mínimo, protege os dados mas MANTÉM a tabela
                if (!setor.exibirResultado()) {
                    // Faz um loop passando por todas as 13 linhas da tabela (Índices 11 ao 23)
                    for (int i = 11; i <= 23; i++) {
                        Row rowAviso = abaSetor.getRow(i);
                        if (rowAviso == null) rowAviso = abaSetor.createRow(i);

                        // Sobrescreve e apaga qualquer dado que estivesse salvo no template original
                        rowAviso.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("DADOS PROTEGIDOS");
                        rowAviso.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("-");
                        rowAviso.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("-");
                    }
                    continue; // Terminou de limpar a tabela? Pula para o próximo setor!
                }

                int linhaAtual = 11;
                for (RiscoFatorDTO fator : setor.fatores()) {
                    Row row = abaSetor.getRow(linhaAtual);
                    if (row == null) row = abaSetor.createRow(linhaAtual);

                    row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(classificarRisco(fator.condicaoAjustada()));
                    row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(classificarRisco(fator.percepcao()));
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