package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.exception.DuplicateEntityException;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.services.EmpresaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;


    @GetMapping
    public ResponseEntity<ResponseDTO<List<Empresa>>> getAllEmpresas() {
        List<Empresa> empresas = empresaService.getAllEmpresas();

        return ResponseEntity.ok(new ResponseDTO<>("Empresas encontradas", empresas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Empresa>> getEmpresasById(@PathVariable UUID id) {
        Empresa empresa = empresaService.getEmpresa(id);

        return ResponseEntity.ok(new ResponseDTO<>("Empresas encontradas", empresa));
    }







    @PostMapping("/criar")
    public ResponseEntity<ResponseDTO<?>> criarEmpresa(@Validated @RequestBody RegistrarEmpresaDTO data){

        try {
            empresaService.criarEmpresa(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Empresa criada com sucesso", null));
        } catch (DuplicateEntityException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO<>(ex.getMessage(), null));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> atualizarEmpresa(@PathVariable UUID id, @Validated @RequestBody AtualizarEmpresaDTO data){

        try {
            empresaService.atualizarEmpresa(id, data);
            return ResponseEntity.ok(new ResponseDTO<>("Empresa atualizada com sucesso", null));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> deletarEmpresa(@PathVariable UUID id){
        try {
            empresaService.deletarEmpresa(id);
            return ResponseEntity.ok(new ResponseDTO<>("Empresa removida com sucesso", null));
        }
        catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>(ex.getMessage(), null));
        }
    }

    @PostMapping("/{id}/gerar-avaliacao-link")
    public ResponseEntity<?> gerarAvaliacaoLink(@PathVariable UUID empresaId, @Validated @RequestParam Integer horasExpiracao) {
        String link = empresaService.gerarLinkAvaliacao(empresaId, horasExpiracao);
        return ResponseEntity.ok(new ResponseDTO<>("Link gerado com sucesso", link));
    }

}
