package com.gabrielbl.healthaplication.infra.security;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {


    @Autowired
    SecurityFilter securityFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Sem permissão");
                        })
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Preflight requests must always pass
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/enviar_link_email").hasRole("ADMIN")

                        // Admin endpoints
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/admin/**").hasRole("ADMIN")


                        // Avaliacoes
                        .requestMatchers(HttpMethod.GET, "/avaliacoes-mensais").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/avaliacoes-mensais/avaliacao/*").hasAnyRole("ADMIN", "RH")
                        .requestMatchers(HttpMethod.POST, "/avaliacoes-mensais/avaliacao/gerar-link").hasAnyRole("ADMIN", "RH")
                        .requestMatchers(HttpMethod.POST, "/avaliacoes-mensais/iniciar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/avaliacoes-mensais/finalizar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/avaliacoes-mensais/*/sinalizar-termino").hasAnyRole("RH", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/avaliacoes-mensais/*/exportar-excel").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/avaliacoes-mensais/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/avaliacoes-mensais/*").hasRole("ADMIN")



                        // Empresa
                        .requestMatchers(HttpMethod.POST, "/empresa/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/empresa/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/empresa/**").hasRole("ADMIN")

                        //Setores
                        .requestMatchers(HttpMethod.GET, "/setores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/setores/*").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.POST, "/setores/criar").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.PUT, "/setores/update").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.DELETE, "/setores/*").hasAnyRole("ADMIN","RH")






                        //Resposta
                        .requestMatchers(HttpMethod.GET, "/resposta/*").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.GET, "/resposta/responder/*").permitAll() // IMPORTANTE
                        .requestMatchers(HttpMethod.POST, "/resposta/responder/*").permitAll() // IMPORTANTE
                        .requestMatchers(HttpMethod.DELETE, "/resposta/responder/*").hasAnyRole("ADMIN","RH")
                        .requestMatchers(HttpMethod.POST, "/resposta/gerar-aleatorios").hasRole("ADMIN")




                        // Authenticated-only access for everything else
                        .anyRequest().authenticated()


                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // specify your frontend origin(s) explicitly
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // if you use cookies or credentials
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // apply to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }





    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
