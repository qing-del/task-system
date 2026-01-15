package com.jacolp.task_system.controller;

import com.jacolp.task_system.dto.AuthResponse;
import com.jacolp.task_system.dto.LoginRequest;
import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.exception.AuthRuntimeException;
import com.jacolp.task_system.mapper.UserMapper;
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

    @Autowired private AuthenticationManager authenticationManager;    // login 验证管家
    @Autowired private UserMapper userMapper;  // 数据库操作
    @Autowired private JwtUtil jwtUtil;    // 签证官
    @Autowired private PasswordEncoder passwordEncoder;    // 密码加密

    /**
     * 注册接口
     * @param request 传入需要注册的用户信息
     * @return 返回是否注册成功
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {
        // 1. 检查用户是否存在
        if(userMapper.countByUsername(request.getUsername()) > 0) {
            log.error("用户名已存在！");
            throw new AuthRuntimeException("用户名已存在！");
        }

        // 2. 创建新用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        // 加密存储密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. 存入数据库
        userMapper.insert(user);

        log.info("用户注册成功！");
        return ResponseEntity.ok("注册成功！");
    }

    /**
     * 登录认证
     * @param request 登录传过来的用户信息
     * @return 登录成功分会Token，否则返回 403
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. 调用 Spring Security 进行认证
        // 如果用户名或者密码错误会直接抛异常，返回 403
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 代码走到这里说明通过了认证
        // 生成 Token
        log.info("用户登录成功！");
        String token = jwtUtil.generateToken(request.getUsername());

        // 3. 返回 Token 给前端
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
