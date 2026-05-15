package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarAdminDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarRhDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UsuarioRepository usuarioRepository,
                        EmpresaRepository empresaRepository,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public void criarEVincularRhParaEmpresa(RegistrarRhDTO data) {

        if(usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Usuário ja cadastrado");

        if(empresaRepository.findByCnpj(data.cnpj()) != null)
            throw new AlreadySubmittedException("CNPJ ja cadastrado");

        Empresa empresa = new Empresa(data.cnpj(),data.nomeEmpresa(),data.emailEmpresa(),null);
        empresaRepository.save(empresa);


        String encryptedPassword = passwordEncoder.encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),empresa,null,null,null);

        usuarioRepository.save(newUsuario);
    }


    public void criarAdmin(RegistrarAdminDTO data) {

        if(usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Usuário ja cadastrado");

        String encryptedPassword = passwordEncoder.encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),null,null,null,null);

        usuarioRepository.save(newUsuario);
    }
}

