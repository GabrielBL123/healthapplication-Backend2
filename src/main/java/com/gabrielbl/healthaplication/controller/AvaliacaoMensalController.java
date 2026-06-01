package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.*;
import com.gabrielbl.healthaplication.services.AvaliacaoMensalService;
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

    public AvaliacaoMensalController(AvaliacaoMensalService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }






    @GetMapping //Retorna todas as avaliacoes
    public ResponseEntity<ResponseDTO<Page<AvaliacaoMensalResponseDTO>>>  getAllAvaliacoesMensal(Pageable pageable){
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
        String tokenGerado = avaliacaoService.gerarLinkAvaliacao(data.cnpj(), data.horasValidade());
        // Ajuste o "localhost:5173" para a porta exata que o seu Vite roda.
        String linkFrontEnd = "http://localhost:5173/home-screen/" + tokenGerado;

        return ResponseEntity.ok(new ResponseDTO<>("Link gerado com sucesso", linkFrontEnd));
    }




    @PostMapping("/iniciar") //Cria e inicia uma avaliacao
    public ResponseEntity<ResponseDTO<?>> iniciarAvaliacaoMensal(@Validated @RequestBody AvaliacaoMensalDTO data) {


        avaliacaoService.criarEIniciarAvaliacaoMensal(data);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" criada e iniciada",null));
    }


    @PostMapping("/finalizar")//Finaliza uma avaliacao
    public ResponseEntity<ResponseDTO<?>> finalizarAvaliacaoMensal(@Validated @RequestBody AvaliacaoMensalDTO data) {

        avaliacaoService.finalizarAvaliacaoMensal(data);



        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" finalizada com sucesso",null));
    }

    @DeleteMapping("/{id}")//Deleta uma avaliacao
    public ResponseEntity<ResponseDTO<?>> deletarAvaliacaoMensal(@PathVariable String id){

        avaliacaoService.deletarAvaliacaoMensal(id);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal deletada com sucesso",null));
    }



















}
