package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalIniciarDTO;
import com.gabrielbl.healthaplication.services.AvaliacaoMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacao_mensal")
public class AvaliacaoMensalController {

    @Autowired
    private AvaliacaoMensalService avaliacaoService;


    /**
     * Criar questionario
     */

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarAvaliacaoMensal(@RequestBody AvaliacaoMensalIniciarDTO data, Authentication authentication) {


        avaliacaoService.criarEIniciarAvaliacaoMensal(data, authentication.getName());

        return ResponseEntity.ok("Avaliacao Mensal de "+ data.competencia() +" criada e iniciada");
    }


    @PostMapping("/finalizar")
    public ResponseEntity<?> finalizarAvaliacaoMensal(@RequestBody String competencia,Authentication authentication){

        avaliacaoService.finalizarAvaliacaoMensal(competencia,authentication.getName());


        return ResponseEntity.ok("Avaliacao Mensal de "+ competencia +" finalizada com sucesso");
    }

    @PostMapping("/deletar")
    public ResponseEntity<?> deletarAvaliacaoMensal(@RequestBody String competencia,Authentication authentication){

        //avaliacaoService.deletarAvaliacaoMensal(competencia,authentication.getName());

        return ResponseEntity.ok("Teste");
    }



















}
