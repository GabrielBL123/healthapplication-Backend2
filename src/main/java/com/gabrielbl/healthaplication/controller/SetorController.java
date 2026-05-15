package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.exception.DuplicateEntityException;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarSetorDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.SetorResponseDTO;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.services.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/setores")
public class SetorController {

    @Autowired
    private SetorService setorService;


    @GetMapping
    public ResponseEntity<ResponseDTO<Page<SetorResponseDTO>>> getAllSetores(Pageable pageable) {

        Page<SetorResponseDTO> setores = setorService.getAllSetores(pageable);

        return ResponseEntity.ok(new ResponseDTO<>("Todos os setores retornados",setores));
    }

    @GetMapping("/{cnpj}")
    public ResponseEntity<ResponseDTO<Page<SetorResponseDTO>>> getSetoresInEmpresa(Pageable pageable,@PathVariable String cnpj) {

        Page<SetorResponseDTO> setores=setorService.getAllEmpresaSetores(cnpj,pageable);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Setores encontrados", setores));


    }

    @PostMapping("/criar")
    public ResponseEntity<ResponseDTO<?>> criarSetor(@Validated @RequestBody RegistrarSetorDTO data,
                                                     Authentication authentication){


        try{
            setorService.criarSetor(data,authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Empresa criada com sucesso", null));

        }catch (DuplicateEntityException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(e.getMessage(),null));
        }



    }


    @PutMapping("/update")
    public ResponseEntity<ResponseDTO<?>> atualizarSetor(@Validated @RequestBody RegistrarSetorDTO data,
                                            Authentication authentication){

        setorService.atualizarSetor(data,authentication.getName());



        return ResponseEntity.ok(new ResponseDTO<>("Setor atualizado com sucesso", null));
    }






}
