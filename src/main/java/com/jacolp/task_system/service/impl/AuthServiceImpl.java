package com.jacolp.task_system.service.impl;

import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.exception.AuthRuntimeException;
import com.jacolp.task_system.mapper.UserMapper;
import com.jacolp.task_system.service.AuthService;
import com.jacolp.task_system.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;    // 密码加密
    @Autowired private AuthenticationManager authenticationManager;    // login 验证管家
    @Autowired private JwtUtil jwtUtil;    // 签证官


    /**
     * 用户注册逻辑
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @Override
    public String login(String username, String password) {
        log.info("Start verifying the login information...");
        // 1. 调用 Spring Security 进行认证
        // 如果用户名或者密码错误会直接抛异常，返回 403
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. 代码走到这里说明通过了认证
        // 生成 Token
        log.info("The user logged in successfully!");
        Long userId = userMapper.findByUsername(username).getId();
        return jwtUtil.generateToken(username, userId);
    }

    /**
     * 用户登录逻辑
     * @param username 用户名
     * @param password 密码
     * @return Token
     */
    @Override
    public String register(String username, String password) {
        // 1. 检查用户是否存在
        if(userMapper.countByUsername(username) > 0) {
            log.error("The user already exists!");
            throw new AuthRuntimeException("The user already exists!");
        }

        // 2. 创建新用户对象
        User user = new User();
        user.setUsername(username);
        // 加密存储密码
        user.setPassword(passwordEncoder.encode(password));



        // 3. 存入数据库
        boolean result = userMapper.insert(user) > 0;
        if (!result) {
            log.error("User registration failed!");
            throw new AuthRuntimeException("User registration failed!");
        }
        log.info("User registration success!");
        return "User registration success!";
    }
}
