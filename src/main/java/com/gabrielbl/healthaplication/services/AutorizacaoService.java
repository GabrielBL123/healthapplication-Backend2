package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.infra.security.TokenService;
import com.gabrielbl.healthaplication.model.DTOs.AutenticacaoDTO;
import com.gabrielbl.healthaplication.model.DTOs.LoginResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class AutorizacaoService {


    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;


    public LoginResponseDTO autenticarUsuario(AutenticacaoDTO data) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());

        var auth = this.authenticationManager.authenticate(usernamePassword);

        // cast principal to your Usuario class (implements UserDetails)
        var principal = (Usuario) auth.getPrincipal();

        // Extract roles from GrantedAuthority (optionally remove "ROLE_" prefix for simpler role names)
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .toList();

        // generate token (TokenService will also include roles claim)
        var accessToken = tokenService.generateToken(principal);


        return  new LoginResponseDTO(accessToken, roles);


    }





    public Cookie createJwtCookie(String token) {

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);  // Prevent JS access
        jwtCookie.setSecure(false);   // Set to true in production (HTTPS required)
        jwtCookie.setPath("/");       // Applies to entire app
        jwtCookie.setMaxAge(3600);    // 1 hour expiry (adjust to match JWT)

        return jwtCookie;
    }


    public void registrarUsuario (RegistrarDTO data) {

        if(usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Login ja registrado");

        Empresa empresa = empresaRepository.findByCnpj(data.empresaCnpj());

        if (empresa == null)
            throw new NotFoundException("Empresa nao encontrada");


        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),empresa,data.cargo(),data.tempoDeTrabalho(),data.jornada());

        usuarioRepository.save(newUsuario);
    }


    public void enviarEmail(String emailFuncionario) {

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

        }



    }






}
