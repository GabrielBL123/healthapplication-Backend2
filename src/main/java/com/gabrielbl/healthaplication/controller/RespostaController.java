package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.GerarLinkDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.services.RespostaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resposta")
public class RespostaController {

    @Autowired
    private RespostaService respostaService;

    @PostMapping("/{token-id}")
    private ResponseEntity<ResponseDTO<?>> submeterResposta(@PathVariable("token-id") String token,
                                                            @Validated @RequestBody RespostaDTO data) {

        respostaService.submeterResposta(data,token);

        return ResponseEntity.ok(new ResponseDTO<>("Resposta submetido com sucesso",null));
    }



}
