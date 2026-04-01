package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.RegisterDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

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

    public void criarRh(RegisterDTO data){

        if(usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Usuário ja cadastrado");


        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Usuario newUsuario = new Usuario(
                data.login(),data.nome(), encryptedPassword, data.role(),null,data.cargo(),data.tempoDeTrabalho(),data.jornada());

        usuarioRepository.save(newUsuario);
    }
}
