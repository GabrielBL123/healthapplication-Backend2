package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.RegistrarRhDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {



    @Autowired
    private AdminService adminService;



    @PostMapping("/criar-rh-empresa")
    ResponseEntity<ResponseDTO<?>> criarRHEmpresa(@RequestBody @Validated RegistrarRhDTO data) {

        adminService.criarEVincularRhParaEmpresa(data);

        return ResponseEntity.ok(new ResponseDTO<>("Rh criado com sucesso", null));

    }



}
