package com.gabrielbl.healthaplication.controller;


import com.gabrielbl.healthaplication.model.DTOs.AtualizarUsuarioResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.ResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.UserResponseDTO;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import com.gabrielbl.healthaplication.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {


    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<ResponseDTO<Page<UserResponseDTO>>> getAllUsers(Pageable pageable) {

        Page<UserResponseDTO> usuarios =  userService.getAllUsuarios(pageable);

        return ResponseEntity.ok().body(new ResponseDTO<>("usuarios", usuarios));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createUser(@RequestBody Usuario usuario) {

        userService.createUser(usuario);

        return ResponseEntity.ok().body(new ResponseDTO<>("usuario", null));
    }




    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> getUserById(@PathVariable String id) {

        UserResponseDTO usuario = userService.getUserById(id);

        return ResponseEntity.ok().body(new ResponseDTO<>("usuario", usuario));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> updateUser(@PathVariable String id,
                                                                   @RequestBody AtualizarUsuarioResponseDTO novosDados) {
        UserResponseDTO usuario = userService.updateUser(id, novosDados);

        return ResponseEntity.ok().body(new ResponseDTO<>("usuario atualizado", usuario));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body(new ResponseDTO<>("usuario deletado", null));
    }


}