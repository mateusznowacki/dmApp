package com.dmApp.controller;

import com.dmApp.dto.AccessTokenDto;
import com.dmApp.dto.LoginRequestDto;
import com.dmApp.dto.RegisterRequestDto;
import com.dmApp.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AccessTokenDto> register(@RequestBody RegisterRequestDto dto, HttpServletResponse response) {
        return authService.register(dto, response);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenDto> login(@RequestBody LoginRequestDto dto, HttpServletResponse response) {
        return authService.login(dto, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenDto> refresh(
            @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) {
        return authService.refresh(refreshToken, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }
}