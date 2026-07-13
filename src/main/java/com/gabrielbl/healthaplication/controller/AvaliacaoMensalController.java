package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.model.DTOs.*;
import com.gabrielbl.healthaplication.services.AvaliacaoMensalService;
import com.gabrielbl.healthaplication.services.RespostaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/avaliacoes-mensais")
public class AvaliacaoMensalController {

    private final AvaliacaoMensalService avaliacaoService;
    private final RespostaService respostaService; // <-- ADICIONADO AQUI

    // CONSTRUTOR ATUALIZADO
    public AvaliacaoMensalController(AvaliacaoMensalService avaliacaoService, RespostaService respostaService) {
        this.avaliacaoService = avaliacaoService;
        this.respostaService = respostaService; // <-- INJETADO AQUI
    }

    @GetMapping //Retorna todas as avaliacoes
    public ResponseEntity<ResponseDTO<Page<AvaliacaoMensalResponseDTO>>> getAllAvaliacoesMensal(Pageable pageable){
        Page<AvaliacaoMensalResponseDTO> avaliacoes = avaliacaoService.getAll(pageable);
        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes",avaliacoes));
    }

    @GetMapping("/{empresa-id}")  //Retorna todas as avaliacoes de determinada empresa(pelo seu ID)
    public ResponseEntity<ResponseDTO<Page<AvaliacaoMensalResponseDTO>>> getAllAvaliacoesMensaisInEmpresa(
            @PathVariable("empresa-id") UUID empresa_id,
            Pageable pageable){
        Page<AvaliacaoMensalResponseDTO> avaliacoes = avaliacaoService.getEmpresaAvaliacoes(pageable,empresa_id);

        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes da empresa",avaliacoes));
    }

    @GetMapping("/avaliacao/{avaliacaoId}") //Retorna As informaçoes de uma avaliacao, como id, competencia, funcionarios, Av.setores, entre outros.
    public ResponseEntity<ResponseDTO<AvaliacaoMensalComSetoresResponseDTO>> getAvaliacao(@PathVariable String avaliacaoId){

        AvaliacaoMensalComSetoresResponseDTO data = avaliacaoService.getAvaliacao(avaliacaoId);
        return ResponseEntity.ok(new ResponseDTO<>("",data));
    }

    @PostMapping("/gerar-link") //POST pois recebe um JSON
    public ResponseEntity<ResponseDTO<?>> gerarLink(@Validated @RequestBody GerarLinkDTO data) {

        // O seu service cria o token único no banco de dados e te devolve ele
        String tokenGerado = avaliacaoService.getLinkAvaliacao(data.cnpj());
        // Ajuste o "localhost:5173" para a porta exata que o seu Vite roda.
        String linkFrontEnd = "http://localhost:5173/home-screen/" + tokenGerado;

        return ResponseEntity.ok(new ResponseDTO<>("Link gerado com sucesso", linkFrontEnd));
    }

    @PostMapping("/iniciar") //Cria e inicia uma avaliacao
    public ResponseEntity<ResponseDTO<?>> iniciarAvaliacaoMensal(@Validated @RequestBody CnpjRequest cnpj) {

        avaliacaoService.criarEIniciarAvaliacaoMensal(cnpj.cnpj());

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de criada e iniciada",null));
    }

    @PostMapping("/finalizar")//Finaliza uma avaliacao
    public ResponseEntity<ResponseDTO<?>> finalizarAvaliacaoMensal(@Validated @RequestBody CnpjRequest cnpj) {

        avaliacaoService.finalizarAvaliacaoMensal(cnpj.cnpj());

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de finalizada com sucesso",null));
    }

    // Rota para o RH avisar que a coleta terminou
    @PostMapping("/{avaliacaoId}/sinalizar-termino")
    public ResponseEntity<ResponseDTO<?>> sinalizarTermino(@PathVariable String avaliacaoId) {
        respostaService.sinalizarTermino(UUID.fromString(avaliacaoId));
        return ResponseEntity.ok(new ResponseDTO<>("Término da avaliação sinalizado com sucesso", null));
    }

    // Rota para o Admin baixar a planilha pronta
    @GetMapping("/{avaliacaoId}/exportar-csv")
    public ResponseEntity<byte[]> exportarCsv(@PathVariable String avaliacaoId) {
        String csv = respostaService.gerarRelatorioCsv(UUID.fromString(avaliacaoId));
        // Formata os bytes usando ISO-8859-1 (ou UTF-8) para o Excel ler acentos do português corretamente
        byte[] csvBytes = csv.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_riscos_nr1.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=ISO-8859-1")
                .body(csvBytes);
    }

    @DeleteMapping("/{id}")//Deleta uma avaliacao
    public ResponseEntity<ResponseDTO<?>> deletarAvaliacaoMensal(@PathVariable String id){

        avaliacaoService.deletarAvaliacaoMensal(id);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal deletada com sucesso",null));
    }

}