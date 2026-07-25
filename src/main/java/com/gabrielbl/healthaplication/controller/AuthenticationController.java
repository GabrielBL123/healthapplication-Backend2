package com.gabrielbl.healthaplication.controller;



import com.gabrielbl.healthaplication.infra.security.TokenService;
import com.gabrielbl.healthaplication.model.DTOs.*;
import com.gabrielbl.healthaplication.services.AutorizacaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AutorizacaoService autorizacaoService;

    private final TokenService tokenService;

    public AuthenticationController(AutorizacaoService autorizacaoService, TokenService tokenService) {
        this.autorizacaoService = autorizacaoService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login") //Faz o login e retorna um token
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody @Validated AutenticacaoRequestDTO data, HttpServletResponse response){

        AutenticarDTO autenticacao = autorizacaoService.autenticar(data);


        Cookie cookie = autorizacaoService.createJwtCookie(autenticacao.refreshToken());

        response.addCookie(cookie);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                autenticacao.accessToken(),
                autenticacao.roles(),
                autenticacao.nome(),
                autenticacao.login(),
                autenticacao.empresaNome(),
                autenticacao.empresaID(),
                autenticacao.usuarioID(),
                autenticacao.avaliacaoAtivaId() // null when user has no active evaluation (e.g., admin role)


        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(new ResponseDTO<>("Login Bem Sucedido", loginResponseDTO ));
    }

    @PostMapping("/registrar") //Inativo
    public ResponseEntity<ResponseDTO<?>> register(@RequestBody @Validated RegistrarDTO data){

        autorizacaoService.registrarUsuario(data);

        return ResponseEntity.ok().build();
    }



    @PostMapping("/enviar_link_email") //Envia Link de registro do Rh por e-mail
    public ResponseEntity<ResponseDTO<?>> enviarLinkDeRegistro(@Validated @RequestBody EnviarConviteRequestDTO data){
        autorizacaoService.enviarEmail(data.email());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<?>> refresh(@CookieValue("refreshToken") String refreshToken) {

        TokensDTO tokens = autorizacaoService.atualizar(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ResponseDTO<>("Token renovado", Map.of("accessToken", tokens.accessToken())));
    }


    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshTokenRaw) {


        String hash = tokenService.hashToken(refreshTokenRaw);

        autorizacaoService.logout(hash);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).build();
    }

}