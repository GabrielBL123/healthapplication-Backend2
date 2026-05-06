package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarUsuarioResponseDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gabrielbl.healthaplication.model.UsuarioFuncao.USER;

@Service
public class UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all users (converts to DTO to hide sensitive data)
     */
    public List<UserResponseDTO> getAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserResponseDTO getUserById(String id) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        return convertToDTO(usuario);

    }

    /**
     * Delete user by ID
     */
    public void deleteUser(String id) {
        if(usuarioRepository.findById(id).isPresent()) {
            usuarioRepository.deleteById(id);
        }
        else  {
            throw new NotFoundException("Usuario nao encontrado");
        }
    }

    /**
     * Business logic: Convert Usuario entity to DTO (hide password)
     */
    private UserResponseDTO convertToDTO(Usuario usuario) {
        Setor setor = usuario.getSetor();
        Empresa empresa = usuario.getEmpresa();

        String usuarioSetorNome = "";

        if(setor != null)
            usuarioSetorNome = "Nenhum";


        return new UserResponseDTO(
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getRole(),
                usuario.getCargo(),
                usuarioSetorNome,
                usuario.getTempoDeTrabalho(),
                usuario.getJornada(),
                empresa.getEmail()

        );
    }

    public Page<UserResponseDTO> getAllUsuarios(Pageable pageable) {

        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);


        return usuarios.map(a -> new UserResponseDTO(a.getLogin(),a.getNome(),
                a.getRole(),a.getCargo(),a.getSetor().getNome(),
                a.getTempoDeTrabalho(),a.getJornada(),a.getEmpresa().getNome()));

    }

    public void createUser(Usuario usuario) {

        if(usuarioRepository.findByLogin(usuario.getLogin())!=null)
            throw new AlreadySubmittedException("Login ja existente");

        usuarioRepository.save(usuario);

    }

    public UserResponseDTO updateUser(String id, AtualizarUsuarioResponseDTO novosDados) {


        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Usuario nao encontrado"));

        usuario.setNome(novosDados.nome());
        usuario.setCargo(novosDados.cargo());
        usuario.setRole(novosDados.role());
        usuario.setLogin(novosDados.login());
        usuario.setJornada(novosDados.jornada());
        usuario.setTempoDeTrabalho(novosDados.tempoDeTrabalho());
        if (novosDados.password() != null && !novosDados.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(novosDados.password()));
        }

        usuarioRepository.save(usuario);

        return convertToDTO(usuario);
    }
}
