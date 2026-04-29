package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.AvaliacaoMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes-mensais")
public class AvaliacaoMensalController {

    @Autowired
    private AvaliacaoMensalService avaliacaoService;


    /**
     * Criar questionario
     */





    @PostMapping("/iniciar")
    public ResponseEntity<ResponseDTO<?>> iniciarAvaliacaoMensal(@RequestBody AvaliacaoMensalDTO data) {


        avaliacaoService.criarEIniciarAvaliacaoMensal(data);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" criada e iniciada",null));
    }


    @PostMapping("/finalizar")
    public ResponseEntity<ResponseDTO<?>> finalizarAvaliacaoMensal(@RequestBody AvaliacaoMensalDTO data) {

        avaliacaoService.finalizarAvaliacaoMensal(data);



        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal de "+ data.competencia() +" finalizada com sucesso",null));
    }

    @PostMapping("/deletar")
    public ResponseEntity<ResponseDTO<?>> deletarAvaliacaoMensal(@RequestBody AvaliacaoMensalDTO data){

        avaliacaoService.deletarAvaliacaoMensal(data);

        return ResponseEntity.ok(new ResponseDTO<>("Avaliacao Mensal deletada com sucesso",null));
    }



















}
