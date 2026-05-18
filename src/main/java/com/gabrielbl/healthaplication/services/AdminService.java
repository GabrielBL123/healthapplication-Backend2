package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.EmpresaResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarAdminDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarRhEEmpresaDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
/*
    public void vincularRhParaEmpresa(String cnpj,String email){
        Empresa empresa = empresaRepository.findByCnpj(cnpj);
        if (empresa == null) {
            throw new NotFoundException("Empresa não encontrada com CNPJ: " + cnpj);
        }

        Usuario usuario = usuarioRepository.findByLogin(email);
        if (usuario == null) {
            throw new NotFoundException("Usuário não encontrado com nome: " + email);
        }

        if (usuario.getEmpresa() != null && usuario.getEmpresa().equals(empresa)) {
            throw new NotFoundException("Usuário já vinculado a esta empresa");
        }

        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }

    public void criarRh(RegistrarDTO data){

        if(usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Usuário ja cadastrado");


        //String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),null,data.cargo(),data.tempoDeTrabalho(),data.jornada());

        usuarioRepository.save(newUsuario);
    }


 */
    @Transactional
    public void criarEVincularRhParaEmpresa(RegistrarRhEEmpresaDTO data){

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

    public Page<RegistrarAdminDTO> getAll(Pageable pageable) {

        Page<Usuario> admins = usuarioRepository.findAll(pageable);

        return admins.map(a ->
                new RegistrarAdminDTO(a.getId(),a.getLogin(),a.getNome(),a.getRole()));
    }


    public void deletarAdmin(String login) {

        Usuario usuario = usuarioRepository.findByLogin(login);
        if(usuario != null)
            usuarioRepository.delete(usuario);
        else throw new NotFoundException("Admin nao encontrado");


    }
}

