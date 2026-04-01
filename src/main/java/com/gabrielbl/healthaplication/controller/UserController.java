package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import com.gabrielbl.healthaplication.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UserController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<UserResponseDTO> informarUsuario(Authentication authentication) {

        return ResponseEntity.ok().body(userService.informarUsuario(authentication));
    }

    // CREATE
    @PostMapping
    public Usuario createUser(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
/*
    // READ ALL
    @GetMapping
    public List<Usuario> getAllUsers() {
        return usuarioRepository.findAll();
    }


 */
    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUserById(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUser(@PathVariable String id, @RequestBody Usuario usuarioDetails) {
        return usuarioRepository.findById(id)
                .map(user -> {
                    user.setLogin(usuarioDetails.getLogin());
                    user.setPassword(usuarioDetails.getPassword());
                    user.setRole(usuarioDetails.getRole());
                    usuarioRepository.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(user -> {
                    usuarioRepository.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


}