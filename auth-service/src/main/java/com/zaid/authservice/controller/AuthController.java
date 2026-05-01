package com.zaid.authservice.controller;

import com.zaid.authservice.dto.request.LoginRequest;
import com.zaid.authservice.dto.request.RegisterRequest;
import com.zaid.authservice.dto.response.AuthResponse;
import com.zaid.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Kayıt işleminde yeni bir kaynak oluşturulduğu için 201 CREATED dönmek best practice'tir.
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Okuma/Giriş işlemlerinde 200 OK dönülür.
        return ResponseEntity.ok(authService.login(request));
    }
}