package com.gabrielbl.healthaplication.infra.security;


import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {


        var token = this.recoverToken(request);
        if(token != null){
            try {

                var userId = tokenService.validateAccessToken(token);
                Usuario user = usuarioRepository.findById(userId).orElse(null);

                if(user != null){

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }


            }

            catch (Exception e) {
                // don't block the request if token is invalid; just continue without authentication
                logger.warn("Invalid token for request {}: {}"+ request.getRequestURI() + e.getMessage());
                


            }
        }





        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        // only search for the accessToken
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
