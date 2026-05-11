package com.gabrielbl.healthaplication.controller;



import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AutenticacaoDTO;
import com.gabrielbl.healthaplication.model.DTOs.LoginResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import com.gabrielbl.healthaplication.infra.security.TokenService;
import com.gabrielbl.healthaplication.services.AutorizacaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {


    @Autowired
    private AutorizacaoService autorizacaoService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody @Validated AutenticacaoDTO data, HttpServletResponse response){

        LoginResponseDTO loginResponseDTO = autorizacaoService.autenticarUsuario(data);
        Cookie cookie = autorizacaoService.createJwtCookie(loginResponseDTO.token());
        response.addCookie(cookie);

        return ResponseEntity.ok(new ResponseDTO<>(loginResponseDTO.token(), loginResponseDTO));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ResponseDTO<?>> register(@RequestBody @Validated RegistrarDTO data){

        autorizacaoService.registrarUsuario(data);

        return ResponseEntity.ok().build();
    }



    // ENVIAR CONVITE PARA FUNCIONÁRIO

    @PostMapping("/enviar_link_email")
    public ResponseEntity<ResponseDTO<?>> enviarLinkDeRegistro(@RequestBody Map<String, String> requestBody){

        String emailFuncionario = requestBody.get("email");

        autorizacaoService.enviarEmail(emailFuncionario);



        return ResponseEntity.ok().build();
    }
}