package com.kinal.user.controller;

import com.kinal.user.dto.request.RegisterInternalRequest;
import com.kinal.user.dto.response.UserInternalResponse;
import com.kinal.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios/internal")
public class UserInternalController {

    private final UserService userService;

    public UserInternalController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterInternalRequest request) {
        String msg = userService.registerInternal(request);
        return ResponseEntity.status(201).body(msg);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserInternalResponse> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getInternalByEmail(email));
    }

    @PutMapping("/{id}/sancionar")
    public ResponseEntity<Void> sancionar(@PathVariable Long id) {
        userService.sancionarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
