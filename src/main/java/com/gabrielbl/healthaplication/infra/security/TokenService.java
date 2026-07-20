package com.gabrielbl.healthaplication.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.gabrielbl.healthaplication.model.RefreshToken;
import com.gabrielbl.healthaplication.model.Usuario;

import com.gabrielbl.healthaplication.repository.RefreshTokenRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class TokenService {


    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${api.security.refreshToken.secret}")
    private String refreshSecret;

    @Value("${api.security.accessToken.secret}")
    private String accessSecret;

    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    public String generateAccessToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(accessSecret);

            List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            LocalDate expiresAt = gerarDataExpiracao(ACCESS_TOKEN_EXPIRATION_MINUTES,ChronoUnit.MINUTES);


            return JWT.create()
                    .withIssuer("healthaplication-api")
                    .withClaim("roles", roles)
                    .withClaim("type", "access")
                    .withSubject(usuario.getId())
                    .withClaim("login", usuario.getLogin())
                    .withExpiresAt(Instant.from(expiresAt))
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }


    public String generateRefreshToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(refreshSecret);

            List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            LocalDate expiresAt = gerarDataExpiracao(REFRESH_TOKEN_EXPIRATION_DAYS,ChronoUnit.DAYS);


            String token = JWT.create()
                    .withIssuer("healthaplication-api")
                    .withClaim("roles", roles)
                    .withClaim("type", "refresh")
                    .withSubject(usuario.getId())
                    .withExpiresAt(Instant.from(expiresAt))
                    .sign(algorithm);


            refreshTokenRepository.save(
                    new RefreshToken(
                            UUID.randomUUID(),
                            hashToken(token),
                            usuario,
                            expiresAt,
                            false
                    )
            );


            return token;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }




    public String validateAccessToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(accessSecret);
            return JWT.require(algorithm)
                    .withIssuer("healthaplication-api")
                    .withClaim("type", "access")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Invalid or expired token", exception);

        }
    }

    public String validateRefreshToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(refreshSecret);
            return JWT.require(algorithm)
                    .withIssuer("healthaplication-api")
                    .withClaim("type", "refresh")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Invalid or expired token", exception);

        }
    }

    private LocalDate gerarDataExpiracao(long total,ChronoUnit tipo) {
        return LocalDate.from(Instant.now().plus(total, tipo));
    }

    public String hashToken(String token) {
        return DigestUtils.sha256Hex(token); // org.apache.commons.codec.digest.DigestUtils
    }


}