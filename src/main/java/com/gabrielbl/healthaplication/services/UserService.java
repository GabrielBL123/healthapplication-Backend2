package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarUsuarioResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.CriarUsuarioDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SetorRepository setorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository,
                       EmpresaRepository empresaRepository,
                       SetorRepository setorRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.setorRepository = setorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO getUserById(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
        return toDTO(usuario);
    }

    public void deleteUser(String id) {
        usuarioRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
        usuarioRepository.deleteById(id);
    }

    public Page<UserResponseDTO> getAllUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::toDTO);
    }

    public void createUser(CriarUsuarioDTO data) {
        if (usuarioRepository.findByLogin(data.login()) != null)
            throw new AlreadySubmittedException("Login ja existente");

        Empresa empresa = null;
        if (data.empresaCnpj() != null && !data.empresaCnpj().isBlank()) {
            empresa = empresaRepository.findByCnpj(data.empresaCnpj());
            if (empresa == null) throw new NotFoundException("Empresa nao encontrada");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        Usuario usuario = new Usuario(data.login(), data.nome(), encryptedPassword, data.role(),
                empresa, data.cargo(), data.tempoDeTrabalho(), data.jornada());
        usuarioRepository.save(usuario);
    }

    public UserResponseDTO updateUser(String id, AtualizarUsuarioResponseDTO novosDados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

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
        return toDTO(usuario);
    }

    private UserResponseDTO toDTO(Usuario usuario) {
        String nomeSetor = usuario.getSetor() != null ? usuario.getSetor().getNome() : "Nenhum";
        String empresaEmail = usuario.getEmpresa() != null ? usuario.getEmpresa().getEmail() : "Não informado";

        // ✨ NOVO: Capturando o CNPJ e o Nome da Empresa de forma segura (evitando NullPointerException)
        String empresaCnpj = usuario.getEmpresa() != null ? usuario.getEmpresa().getCnpj() : "Não informado";
        // Nota: Se na sua classe Empresa.java o método for "getNomeEmpresa()", ajuste a linha abaixo!
        String empresaNome = usuario.getEmpresa() != null ? usuario.getEmpresa().getNome() : "Não informado";

        return new UserResponseDTO(
                usuario.getLogin(),
                usuario.getNome(),
                "", // A senha não deve ser retornada por segurança
                usuario.getRole(),
                empresaEmail,
                empresaCnpj,   // ✨ Campo novo passado para o DTO
                empresaNome,   // ✨ Campo novo passado para o DTO
                usuario.getCargo(),
                nomeSetor,
                usuario.getTempoDeTrabalho(),
                usuario.getJornada()
        );
    }
}