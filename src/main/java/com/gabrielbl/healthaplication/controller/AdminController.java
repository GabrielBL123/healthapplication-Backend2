package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.RegistrarAdminDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarRhEEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping
    public ResponseEntity<ResponseDTO<Page<RegistrarAdminDTO>>> getAllAdmin(Pageable pageable) {

        Page<RegistrarAdminDTO> admins = adminService.getAll(pageable);

        return ResponseEntity.ok(new ResponseDTO<>("",admins));
    }

    @PostMapping("/criar-rh-empresa")
    ResponseEntity<ResponseDTO<RegistrarRhEEmpresaDTO>> criarRHEmpresa(@RequestBody @Validated RegistrarRhEEmpresaDTO data) {

        adminService.criarEVincularRhParaEmpresa(data);

        return ResponseEntity.ok(new ResponseDTO<>("Rh criado com sucesso", data));

    }

    @PostMapping("/criar_admin")
    ResponseEntity<ResponseDTO<RegistrarAdminDTO>> criarAdmin(@RequestBody @Validated RegistrarAdminDTO data) {

        adminService.criarAdmin(data);

        return ResponseEntity.ok(new ResponseDTO<>("Admin criado com sucesso", data));
    }

    @DeleteMapping()
    ResponseEntity<ResponseDTO<RegistrarAdminDTO>> deletarAdmin(@RequestParam String login){



        adminService.deletarAdmin(login);


        return ResponseEntity.ok(new ResponseDTO<>("Usuario deletado com sucesso",null));
    }




}
