package com.dmApp.service;


import com.dmApp.dto.AccessTokenDto;
import com.dmApp.dto.LoginRequestDto;
import com.dmApp.dto.RegisterRequestDto;
import com.dmApp.entity.User;
import com.dmApp.enums.Role;
import com.dmApp.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordService passwordService;


    public AuthService(JwtUtil jwtUtil, UserService userService, PasswordService passwordService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordService = passwordService;
    }

    public ResponseEntity<AccessTokenDto> register(RegisterRequestDto Dto, HttpServletResponse response) {
        if (userService.existsByEmail(Dto.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        User user = new User();
        user.setEmail(Dto.email());
        user.setPassword(passwordService.encodePassword(Dto.password()));
        user.setUsername(Dto.username());
        user.setRole(Role.USER);

        userService.registerUser(user);

        String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getId()));

        addCookieToResponse("refresh_token", refreshToken, response);

        return ResponseEntity.ok(new AccessTokenDto(accessToken));
    }


    public ResponseEntity<AccessTokenDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String email = loginRequestDto.email();
        String password = loginRequestDto.password();

        if (userService.isValidUser(email, password)) {
            User user = userService.findByEmail(email).orElseThrow();
             String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getId()));

            addCookieToResponse("refresh_token", refreshToken, response);

            return ResponseEntity.ok(new AccessTokenDto(accessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<AccessTokenDto> refresh(String refreshToken, HttpServletResponse response) {
        if (jwtUtil.validateToken(refreshToken)) {
            String userId = jwtUtil.extractUserId(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            addCookieToResponse("refresh_token", newRefreshToken, response);

            return ResponseEntity.ok(new AccessTokenDto(newAccessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ZMIEŃ NA true w produkcji
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    private void addCookieToResponse(String cookieName, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ZMIEŃ NA `true` w środowisku produkcyjnym!
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 3600); // 7 dni
        response.addCookie(cookie);
    }
}