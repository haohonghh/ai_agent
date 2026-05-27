package com.smartcare.controller;

import com.smartcare.dto.ApiResponse;
import com.smartcare.dto.LoginRequest;
import com.smartcare.model.entity.User;
import com.smartcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword())
                .map(user -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", "jwt-token-placeholder");
                    data.put("expiresIn", 86400);
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("role", user.getRole());
                    data.put("user", userInfo);
                    return ApiResponse.success(data);
                })
                .orElse(ApiResponse.error(40101, "用户名或密码错误"));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        try {
            User user = authService.register(
                    params.get("username"),
                    params.get("password"),
                    params.get("email"),
                    params.get("phone"),
                    params.get("role")
            );
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            return ApiResponse.success(userInfo);
        } catch (IllegalArgumentException e) {
            return ApiResponse.paramError(e.getMessage());
        }
    }
}