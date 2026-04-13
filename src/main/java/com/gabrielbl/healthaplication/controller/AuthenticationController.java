package com.gabrielbl.healthaplication.controller;



import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AuthenticationDTO;
import com.gabrielbl.healthaplication.model.DTOs.LoginResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegisterDTO;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import com.gabrielbl.healthaplication.infra.security.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;


    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository emrpesaRepository;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated AuthenticationDTO data, HttpServletResponse response){

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // cast principal to your Usuario class (implements UserDetails)
        var principal = (Usuario) auth.getPrincipal();

        // Extract roles from GrantedAuthority (optionally remove "ROLE_" prefix for simpler role names)
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .collect(Collectors.toList());

        // generate token (TokenService will also include roles claim)
        var accessToken = tokenService.generateToken(principal);

        Cookie jwtCookie = new Cookie("jwt", accessToken);
        jwtCookie.setHttpOnly(true);  // Prevent JS access
        jwtCookie.setSecure(false);   // Set to true in production (HTTPS required)
        jwtCookie.setPath("/");       // Applies to entire app
        jwtCookie.setMaxAge(3600);    // 1 hour expiry (adjust to match JWT)
        response.addCookie(jwtCookie);

        // Return token (optional) and roles so frontend knows the user's roles
        // If you don't want to return the token in the body, remove it here but keep roles.
        return ResponseEntity.ok(new LoginResponseDTO(accessToken, roles));
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO data){
        if(this.usuarioRepository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().body("Login existente");
        // Find the Empresa by CNPJ (or by ID)
        Empresa empresa = this.emrpesaRepository.findByCnpj(data.empresaCnpj());
        if (empresa == null)
            return ResponseEntity.badRequest().body("Empresa não encontrada");


        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),empresa,data.cargo(),data.tempoDeTrabalho(),data.jornada());

        this.usuarioRepository.save(newUsuario);

        return ResponseEntity.ok().build();
    }



    // ENVIAR CONVITE PARA FUNCIONÁRIO

    @PostMapping("/enviar_link_email")
    public ResponseEntity<?> enviarLinkDeRegistro(@RequestBody java.util.Map<String, String> requestBody){

        String emailFuncionario = requestBody.get("email");



        //Dispara o e-mail
        try {

            String linkSistema = "http://localhost:5173/completar-cadastro?email=" + java.net.URLEncoder.encode(emailFuncionario, "UTF-8");

            org.springframework.mail.SimpleMailMessage email = new org.springframework.mail.SimpleMailMessage();
            email.setTo(emailFuncionario);
            email.setSubject("Convite para o Sistema Cuida+");
            email.setText("Olá!\n\n" +
                    "Você foi convidado pelo RH para acessar o Sistema Cuida+ da sua empresa.\n" +
                    "Para criar a sua senha e completar o seu cadastro, clique no link abaixo:\n\n" +
                    linkSistema + "\n\n" +
                    "Seja bem-vindo(a)!");

            mailSender.send(email);
            System.out.println("Convite enviado com sucesso para: " + emailFuncionario);

        } catch (Exception e) {
            System.out.println("Erro ao enviar o e-mail: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Falha ao enviar o convite.");
        }

        return ResponseEntity.ok().build();
    }
}