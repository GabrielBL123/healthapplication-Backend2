package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.exception.UnauthorizedException;
import com.gabrielbl.healthaplication.infra.security.TokenService;
import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.AutenticacaoDTO;
import com.gabrielbl.healthaplication.model.DTOs.LoginResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarDTO;
import com.gabrielbl.healthaplication.model.DTOs.TokenPairDTO;
import com.gabrielbl.healthaplication.repository.AvaliacaoMensalRepository;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.RefreshTokenRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutorizacaoService {

    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AutorizacaoService(@Lazy AuthenticationManager authenticationManager,
                              JavaMailSender mailSender,
                              TokenService tokenService,
                              UsuarioRepository usuarioRepository,
                              EmpresaRepository empresaRepository,
                              AvaliacaoMensalRepository avaliacaoMensalRepository,
                              RefreshTokenRepository refreshTokenRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.mailSender = mailSender;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public TokenPairDTO autenticarUsuario(AutenticacaoDTO data) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());

        var auth = this.authenticationManager.authenticate(usernamePassword);

        // cast principal to your Usuario class (implements UserDetails)
        var principal = (Usuario) auth.getPrincipal();

        // generate token (TokenService will also include roles claim)
        var accessToken = tokenService.generateAccessToken(principal);
        var refreshToken = tokenService.generateRefreshToken(principal);

        //também salva o refreshToken no banco de dados



        return  new  TokenPairDTO(
                accessToken,
                refreshToken
        );


    }





    public Cookie createJwtCookie(String token) {

        Cookie jwtCookie = new Cookie("refreshToken", token);
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

            String linkSistema = "http://localhost:5173/login";

            org.springframework.mail.SimpleMailMessage email = new org.springframework.mail.SimpleMailMessage();
            email.setTo(emailFuncionario);
            email.setSubject("Convite para o Sistema Cuida+");
            email.setText("Olá!\n\n" +
                    "Você foi convidado para acessar login.\n" +
                    "Clique no link abaixo:\n\n" +
                    linkSistema + "\n\n" +
                    "Seja bem-vindo(a)!");

            mailSender.send(email);
            System.out.println("Convite enviado com sucesso para: " + emailFuncionario);

        } catch (Exception e) {
            System.out.println("Erro ao enviar o e-mail: " + e.getMessage());

        }



    }


    @Transactional
    public TokenPairDTO atualizar(String refreshTokenRaw) {

        // 1. Verify JWT signature/expiration first

        String usuarioId = tokenService.validateRefreshToken(refreshTokenRaw);
        if (usuarioId == null) {
            throw new UnauthorizedException("Refresh token inválido ou expirado");
        }

        // 2. Check it exists, isn't revoked, and hasn't expired in the DB
        String tokenHash = tokenService.hashToken(refreshTokenRaw);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token não reconhecido"));

        if (storedToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token revogado");
        }

        if(storedToken.getExpiresAt().isBefore(Instant.now())){
            throw new RuntimeException("Refresh token expirado");
        }


        // 3. Rotate: revoke the old one, issue new access + refresh tokens


        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        String newAccessToken = tokenService.generateAccessToken(usuario);
        String newRefreshToken = tokenService.generateRefreshToken(usuario);


        return new TokenPairDTO(newAccessToken, newRefreshToken);
    }




    public void logout(String hash) {

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash).orElseThrow();

        refreshTokenRepository.revokeAllByUsuarioId(refreshToken.getUsuario().getId());
    }
}
