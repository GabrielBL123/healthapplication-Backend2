package com.gabrielbl.healthaplication.controller;



import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AutenticacaoDTO;
import com.gabrielbl.healthaplication.model.DTOs.EnviarConviteDTO;
import com.gabrielbl.healthaplication.model.DTOs.LoginResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.services.AutorizacaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AutorizacaoService autorizacaoService;

    public AuthenticationController(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @PostMapping("/login") //Faz o login e retorna um token
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody @Validated AutenticacaoDTO data, HttpServletResponse response){

        LoginResponseDTO loginResponseDTO = autorizacaoService.autenticarUsuario(data);
        Cookie cookie = autorizacaoService.createJwtCookie(loginResponseDTO.token());
        response.addCookie(cookie);

        return ResponseEntity.ok(new ResponseDTO<>(loginResponseDTO.token(), loginResponseDTO));
    }

    @PostMapping("/registrar") //Inativo
    public ResponseEntity<ResponseDTO<?>> register(@RequestBody @Validated RegistrarDTO data){

        autorizacaoService.registrarUsuario(data);

        return ResponseEntity.ok().build();
    }



    @PostMapping("/enviar_link_email") //Envia Link de registro do Rh por e-mail
    public ResponseEntity<ResponseDTO<?>> enviarLinkDeRegistro(@Validated @RequestBody EnviarConviteDTO data){
        autorizacaoService.enviarEmail(data.email());
        return ResponseEntity.ok().build();
    }
}