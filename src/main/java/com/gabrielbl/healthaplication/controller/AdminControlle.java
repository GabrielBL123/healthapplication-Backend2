package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.RegisterDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegisterRhDTO;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import com.gabrielbl.healthaplication.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminControlle {



    @Autowired
    private AdminService adminService;

    public record VincularRHRequest(String cnpj, String login) {}


    @PostMapping("/criar_rh_empresa")
    ResponseEntity<?> criarRHEmpresa(@RequestBody @Validated RegisterRhDTO data) {

        adminService.criarEVincularRhParaEmpresa(data);

        return ResponseEntity.ok("Rh e Empresa Criada com sucesso");

    }
    /*
    @PostMapping("/registrar_rh")
    ResponseEntity<?> criarRH(@RequestBody @Validated RegisterDTO data) {

        adminService.criarRh(data);



        return ResponseEntity.ok().build();

    }

    @PostMapping("/vincular_rh_a_empresa/")
    ResponseEntity<?> vincularRH(@RequestBody @Validated VincularRHRequest requestRH) {

        adminService.vincularRhParaEmpresa(requestRH.cnpj, requestRH.login);

        return ResponseEntity.notFound().build();
    }

     */



}
