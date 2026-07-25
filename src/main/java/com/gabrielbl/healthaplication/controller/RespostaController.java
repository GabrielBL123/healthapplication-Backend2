package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.*;
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
    public ResponseEntity<ResponseDTO<Page<ListaRespostaResponseDTO>>> getTodosQResponderam(@PathVariable String empresaId, Pageable pageable){

        Page<ListaRespostaResponseDTO> respostaData = respostaService.getAllRespostaInfo(empresaId,pageable);

       return ResponseEntity.ok((new  ResponseDTO<>("",respostaData)));

    }


    //Retorna o cnpj o nome da empresa com o nome dos setores
    //Sera usado quando o usuario acessar o link e começar a responder o questionario
    @GetMapping("/responder/{token-id}")
    public ResponseEntity<ResponseDTO<RespostaInfoEmpresaDTO>> getRespostaInfo(@PathVariable("token-id") String tokenId){
        RespostaInfoEmpresaDTO data = respostaService.getRespostaInfoEmpresa(tokenId);
        return ResponseEntity.ok(new ResponseDTO<>("", data));
    }


    //Quando o usuario envia a sua resposta
    @PostMapping("/responder/{token-id}")
    public ResponseEntity<ResponseDTO<?>> submeterResposta(@PathVariable("token-id") String token,
                                                            @Validated @RequestBody RespostaDTO data) {
        System.out.println("O DTO CHEGOU PERFEITAMENTE: " + data);
        respostaService.submeterResposta(data,token);

        return ResponseEntity.ok(new ResponseDTO<>("Resposta submetido com sucesso",null));
    }




    @PostMapping("/gerar-aleatorios")
    public ResponseEntity<ResponseDTO<?>> gerarRespostasAleatorias(
            @Validated @RequestBody GerarRespostasDTO data) {

        respostaService.gerarRespostasAleatorias(data.quantidade());
        return ResponseEntity.ok(new ResponseDTO<>(
                "Geradas " + data.quantidade() + " respostas aleatórias com sucesso",
                null
        ));
    }


}
