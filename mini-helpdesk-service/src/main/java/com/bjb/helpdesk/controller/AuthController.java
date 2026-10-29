package com.bjb.helpdesk.controller;

import com.bjb.helpdesk.dto.ApiResponse;
import com.bjb.helpdesk.dto.request.LoginRequest;
import com.bjb.helpdesk.dto.request.RefreshTokenRequest;
import com.bjb.helpdesk.dto.request.RegisterRequest;
import com.bjb.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        var data = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registrasi berhasil", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        var data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login berhasil", data));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var data = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok("Token diperbarui", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Logout berhasil", null));
    }
}
