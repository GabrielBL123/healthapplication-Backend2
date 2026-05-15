package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalDTO;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.GerarLinkDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
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


    /**
     * Criar questionario
     */



    @GetMapping
    public ResponseEntity<ResponseDTO<Page<AvaliacaoMensalResponseDTO>>>  getAllAvaliacoesMensal(Pageable pageable){
        Page<AvaliacaoMensalResponseDTO> avaliacoes = avaliacaoService.getAll(pageable);
        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes",avaliacoes));
    }

    @GetMapping("/{empresa-id}")
    public ResponseEntity<ResponseDTO<Page<AvaliacaoMensalResponseDTO>>> getAllAvaliacoesMensaisInEmpresa(
            @PathVariable("empresa-id") UUID empresa_id,
            Pageable pageable){
        Page<AvaliacaoMensalResponseDTO> avaliacoes = avaliacaoService.getEmpresaAvaliacoes(pageable,empresa_id);

        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes da empresa",avaliacoes));
    }

    @GetMapping("/gerar-link")
    private ResponseEntity<ResponseDTO<?>> gerarLink(@Validated @RequestBody GerarLinkDTO data) {

        String link = avaliacaoService.gerarLink(data);
        return ResponseEntity.ok(new ResponseDTO<>("link gerado com sucesso",link));
    }




    @PostMapping("/iniciar")
    public ResponseEntity<ResponseDTO<?>> iniciarAvaliacaoMensal(@Validated @RequestBody AvaliacaoMensalDTO data) {


        avaliacaoService.criarEIniciarAvaliacaoMensal(data);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" criada e iniciada",null));
    }


    @PostMapping("/finalizar")
    public ResponseEntity<ResponseDTO<?>> finalizarAvaliacaoMensal(@Validated @RequestBody AvaliacaoMensalDTO data) {

        avaliacaoService.finalizarAvaliacaoMensal(data);



        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" finalizada com sucesso",null));
    }

    @PostMapping("/deletar")
    public ResponseEntity<ResponseDTO<?>> deletarAvaliacaoMensal(@Validated @RequestBody AvaliacaoMensalDTO data){

        avaliacaoService.deletarAvaliacaoMensal(data);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal deletada com sucesso",null));
    }



















}
