package com.gabrielbl.healthaplication.infra.config;


import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:admin@teste.com}")
    private String login;

    @Value("${admin.default.senha:admin123}")
    private String adminSenha;

    public AdminUserSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByLogin(login) != null) {
            return; // already seeded, keep it idempotent
        }

        Usuario admin = new Usuario();
        admin.setLogin(login);
        admin.setPassword(passwordEncoder.encode(adminSenha));
        admin.setRole(UsuarioFuncao.ADMIN);
        admin.setNome("Admin");

        usuarioRepository.save(admin);

        System.out.printf("Default admin user created: %s%n", login);
    }
}