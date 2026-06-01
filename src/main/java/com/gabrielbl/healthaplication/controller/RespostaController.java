package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.model.DTOs.ListaRespostaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RespostaInfoEmpresaDTO;
import com.gabrielbl.healthaplication.services.RespostaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resposta")
public class RespostaController {

    @Autowired
    private RespostaService respostaService;



    //Retorna todos os funcionários que responderam à avaliação ativa de determinada empresa(pelo ID da empresa informado)
    @GetMapping("/{empresa-id}")
    public ResponseEntity<ResponseDTO<Page<ListaRespostaDTO>>> getTodosQResponderam(@PathVariable String empresaId, Pageable pageable){

        Page<ListaRespostaDTO> respostaData = respostaService.getAllRespostaInfo(empresaId,pageable);

       return ResponseEntity.ok((new  ResponseDTO<>("",respostaData)));

    }


    //Retorna o cnpj o nome da empresa com o nome dos setores
    //Sera usado quando o usuario acessar o link e começar a responder o questionario
    @GetMapping("/{token-id}")
    private ResponseEntity<ResponseDTO<RespostaInfoEmpresaDTO>> getRespostaInfo(@PathVariable String tokenId){

        RespostaInfoEmpresaDTO data = respostaService.getRespostaInfoEmpresa(tokenId);

        return ResponseEntity.ok(new ResponseDTO<>("",null));
    }


    //Quando o usuario envia a sua resposta
    @PostMapping("/{token-id}")
    private ResponseEntity<ResponseDTO<?>> submeterResposta(@PathVariable("token-id") String token,
                                                            @Validated @RequestBody RespostaDTO data) {

        respostaService.submeterResposta(data,token);

        return ResponseEntity.ok(new ResponseDTO<>("Resposta submetido com sucesso",null));
    }



}
