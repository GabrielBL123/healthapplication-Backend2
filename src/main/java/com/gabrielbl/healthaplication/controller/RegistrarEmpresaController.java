package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.RegisterCompanyDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegisterSetorDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.services.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/registrar")
public class RegistrarEmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private SetorService setorService;



    @PostMapping("/empresa")
    public ResponseEntity<?> registrarEmpresa(@RequestBody @Validated RegisterCompanyDTO data){
        if(this.empresaRepository.findByCnpj(data.cnpj()) != null) return ResponseEntity.badRequest().build();

        Empresa empresa = new Empresa(data.cnpj(),data.nome(),data.email(),data.telefone());

        this.empresaRepository.save(empresa);

        return ResponseEntity.ok().build();

    }
    @PostMapping("/setor")
    public ResponseEntity<?> registrarSetor(@RequestBody @Validated RegisterSetorDTO data, Authentication authentication){


        //Registra um setor com base na informaçao sobre a empresa do usuario.

        setorService.criarSetor(data, authentication.getName());

        return ResponseEntity.ok().build();


    }
}
