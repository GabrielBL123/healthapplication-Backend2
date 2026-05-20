package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.model.DTOs.RegistrarSetorDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.SetorResponseDTO;
import com.gabrielbl.healthaplication.services.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/setores")
public class SetorController {

    private final SetorService setorService;

    public SetorController(SetorService setorService) {
        this.setorService = setorService;
    }


    @GetMapping //Retorna todos os setores de todas as empresas(Muita informação)
    public ResponseEntity<ResponseDTO<Page<SetorResponseDTO>>> getAllSetores(Pageable pageable) {

        Page<SetorResponseDTO> setores = setorService.getAllSetores(pageable);

        return ResponseEntity.ok(new ResponseDTO<>("Todos os setores retornados",setores));
    }

    @GetMapping("/{cnpj}") //Retorna todos os setores de uma determinada empresa
    public ResponseEntity<ResponseDTO<Page<SetorResponseDTO>>> getSetoresInEmpresa(Pageable pageable,@PathVariable String cnpj) {

        Page<SetorResponseDTO> setores=setorService.getAllEmpresaSetores(cnpj,pageable);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Setores encontrados", setores));


    }
    //Precisa de melhorias
    @PostMapping("/criar") //Cria um setor(com o nome do setor e o cnpj informados no JSON)
    public ResponseEntity<ResponseDTO<?>> criarSetor(@Validated @RequestBody RegistrarSetorDTO data,
                                                     Authentication authentication){
        setorService.criarSetor(data, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Setor criado com sucesso", null));
    }


    @PutMapping("/update") //Atualiza o setor(com o nome do setor e o cnpj informados no JSON)
    public ResponseEntity<ResponseDTO<?>> atualizarSetor(@Validated @RequestBody RegistrarSetorDTO data,
                                            Authentication authentication){

        setorService.atualizarSetor(data,authentication.getName());



        return ResponseEntity.ok(new ResponseDTO<>("Setor atualizado com sucesso", null));
    }






}
