package com.jacolp.task_system.controller;

import com.jacolp.task_system.dto.AuthResponse;
import com.jacolp.task_system.dto.LoginRequest;
import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.exception.AuthRuntimeException;
import com.jacolp.task_system.mapper.UserMapper;
import com.jacolp.task_system.service.AuthService;
import com.jacolp.task_system.service.UserService;
import com.jacolp.task_system.utils.JwtUtil;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {
    @Autowired private AuthService authService;

    /**
     * 注册接口
     * @param request 传入需要注册的用户信息
     * @return 返回是否注册成功
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        log.info("The user starts to register...");

        String result = authService.register(username, password);

        log.info("The user registered successfully!");
        return ResponseEntity.ok(result);
    }

    /**
     * 登录认证
     * @param request 登录传过来的用户信息
     * @return 登录成功分会Token，否则返回 403
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. 先获取用户名和密码
        String username = request.getUsername();
        String password = request.getPassword();

        log.info("The user starts to login...");

        // 2. 调用 AuthService 进行登录认证
        String token = authService.login(username, password);

        // 3. 返回 Token 给前端
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
