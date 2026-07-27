package com.gabrielbl.healthaplication.controller;



import com.gabrielbl.healthaplication.infra.security.TokenService;
import com.gabrielbl.healthaplication.model.DTOs.*;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.services.AutorizacaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AutorizacaoService autorizacaoService;

    private final TokenService tokenService;

    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(7);

    public AuthenticationController(AutorizacaoService autorizacaoService, TokenService tokenService) {
        this.autorizacaoService = autorizacaoService;
        this.tokenService = tokenService;
    }

    private ResponseCookie buildRefreshCookie(String value,Duration maxAge){
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")// safest: matches every request path regardless of context-path
                .maxAge(maxAge)
                .build();
    }

    @PostMapping("/login") //Faz o login e retorna um token
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody @Validated AutenticacaoRequestDTO data, HttpServletResponse response){

        AutenticarDTO autenticacao = autorizacaoService.autenticar(data);


        ResponseCookie cookie = buildRefreshCookie(autenticacao.refreshToken(), REFRESH_TOKEN_MAX_AGE);



        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                autenticacao.accessToken(),
                autenticacao.roles(),
                autenticacao.nome(),
                autenticacao.login(),
                autenticacao.empresaNome(), //null if the user is an admin
                autenticacao.empresaID(), //null if the user is an admin
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

        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken(), REFRESH_TOKEN_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ResponseDTO<>("Token renovado", Map.of("accessToken", tokens.accessToken())));
    }


    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<PerfilDTO>> me() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuarioLogado)) {
            return ResponseEntity.status(401).body(new ResponseDTO<>("Não autenticado", null));
        }
        PerfilDTO perfil = autorizacaoService.buscarPerfilUsuario(usuarioLogado);
        return ResponseEntity.ok(new ResponseDTO<>("Perfil do usuário", perfil));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshTokenRaw) {


        String hash = tokenService.hashToken(refreshTokenRaw);

        autorizacaoService.logout(hash);

        ResponseCookie deleteCookie = buildRefreshCookie("", Duration.ZERO);


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                deleteCookie.toString()).build();
    }

}