package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Optional<UserResponseDTO> getUserById(String id) {
        return usuarioRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Delete user by ID
     */
    public boolean deleteUser(String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
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
                usuario.getRole().name(),
                usuario.getCargo(),
                usuarioSetorNome,
                usuario.getTempoDeTrabalho(),
                usuario.getJornada(),
                empresa.getEmail()

        );
    }

    public UserResponseDTO informarUsuario(Authentication authentication) {

        Usuario usuario = usuarioRepository.findByLogin(authentication.getName());

        if(usuario == null) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        return convertToDTO(usuario);

    }

    public UserResponseDTO createUser(Usuario usuario) {
        Usuario saved = usuarioRepository.save(usuario);
        return convertToDTO(saved);
    }

    public Optional<UserResponseDTO> updateUser(String id, Usuario userDetails) {
        return usuarioRepository.findById(id).map(user -> {
            user.setLogin(userDetails.getLogin());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            user.setRole(userDetails.getRole());
            return convertToDTO(usuarioRepository.save(user));
        });
    }
}
