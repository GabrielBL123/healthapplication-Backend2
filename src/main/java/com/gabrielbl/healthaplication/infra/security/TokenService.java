package com.gabrielbl.healthaplication.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gabrielbl.healthaplication.model.Usuario;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import java.util.stream.Collectors;


@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    public String generateAccessToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);

            List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            return JWT.create()
                    .withIssuer("healthaplication-api")
                    .withClaim("roles", roles)
                    .withSubject(usuario.getId())
                    .withClaim("login", usuario.getLogin())
                    .withExpiresAt(gerarDataExpiracao(ACCESS_TOKEN_EXPIRATION_MINUTES))
                    .sign(algorithm)
                    ;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }


    public String generateRefreshToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);

            List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            return JWT.create()
                    .withIssuer("healthaplication-api")
                    .withClaim("roles", roles)
                    .withSubject(usuario.getId())
                    .withClaim("type", "refresh")
                    .withExpiresAt(gerarDataExpiracao(REFRESH_TOKEN_EXPIRATION_DAYS))
                    .sign(algorithm)
                    ;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }




    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("healthaplication-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Invalid or expired token", exception);

        }
    }

    private Date gerarDataExpiracao(long minutes) {
        return Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    public String hashToken(String token) {
        return DigestUtils.sha256Hex(token); // org.apache.commons.codec.digest.DigestUtils
    }
}