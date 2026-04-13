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
    public ResponseEntity<String> iniciarAvaliacaoMensal(@RequestBody AvaliacaoMensalIniciarDTO data) {


        avaliacaoService.criarEIniciarAvaliacaoMensal(data);

        return ResponseEntity.ok("Avaliacao Mensal de "+ data.competencia() +" criada e iniciada");
    }


    @PostMapping("/finalizar")
    public ResponseEntity<?> finalizarAvaliacaoMensal(@RequestBody AvaliacaoMensalIniciarDTO data) {

        avaliacaoService.finalizarAvaliacaoMensal(data);



        return ResponseEntity.ok("Avaliacao Mensal de "+ data.competencia() +" finalizada com sucesso");
    }

    @PostMapping("/deletar")
    public ResponseEntity<?> deletarAvaliacaoMensal(@RequestBody AvaliacaoMensalIniciarDTO data){

        avaliacaoService.deletarAvaliacaoMensal(data);

        return ResponseEntity.ok("Avaliacao Mensal deletada com sucesso");
    }



















}
