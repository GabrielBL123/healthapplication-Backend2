package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.EmpresaResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }


    @GetMapping
    public ResponseEntity<ResponseDTO<Page<EmpresaResponseDTO>>> getAllEmpresas(Pageable pageable) {

        Page<EmpresaResponseDTO> empresas = empresaService.getAllEmpresas(pageable);
        return ResponseEntity.ok(new ResponseDTO<>("Empresas encontradas", empresas));
    }


    @PostMapping("/criar")
    public ResponseEntity<ResponseDTO<?>> criarEmpresa(@Validated @RequestBody RegistrarEmpresaDTO data){
        empresaService.criarEmpresa(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Empresa criada com sucesso", null));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> atualizarEmpresa(@PathVariable UUID id, @Validated @RequestBody AtualizarEmpresaDTO data){
        empresaService.atualizarEmpresa(id, data);
        return ResponseEntity.ok(new ResponseDTO<>("Empresa atualizada com sucesso", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> deletarEmpresa(@PathVariable UUID id){
        empresaService.deletarEmpresa(id);
        return ResponseEntity.ok(new ResponseDTO<>("Empresa removida com sucesso", null));
    }

}
