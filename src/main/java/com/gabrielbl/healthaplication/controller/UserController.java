package com.gabrielbl.healthaplication.controller;

import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<UserResponseDTO> informarUsuario(Authentication authentication) {
        return ResponseEntity.ok().body(userService.informarUsuario(authentication));
    }

    // CREATE
    @PostMapping
    public UserResponseDTO createUser(@RequestBody Usuario usuario) {
        return userService.createUser(usuario);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id, @RequestBody Usuario usuarioDetails) {
        return userService.updateUser(id, usuarioDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok().<Void>build();
        }
        return ResponseEntity.notFound().build();
    }

}
