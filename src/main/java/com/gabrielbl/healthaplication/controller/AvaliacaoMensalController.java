package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.AvaliacaoSetor;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.AvaliacaoMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/avaliacoes-mensais")
public class AvaliacaoMensalController {

    @Autowired
    private AvaliacaoMensalService avaliacaoService;


    /**
     * Criar questionario
     */



    @GetMapping
    public ResponseEntity<ResponseDTO<List<AvaliacaoMensal>>>  getAllAvaliacoesMensal(){
        List<AvaliacaoMensal> avaliacoes = avaliacaoService.getAll();
        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes",avaliacoes));
    }

    @GetMapping("/{empresa-id}")
    public ResponseEntity<ResponseDTO<List<AvaliacaoMensal>>> getEmpresaAllAvaliacoesMensais(
            @PathVariable("empresa-id") UUID empresa_id){
        List<AvaliacaoMensal> avaliacoes = avaliacaoService.getEmpresaAvaliacoes(empresa_id);

        return ResponseEntity.ok(new ResponseDTO<>("Lista de todas as avaliacoes da empresa",avaliacoes));
    }




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
