package com.jacolp.task_system.controller;

import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.dto.UserRequest;
import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {
    /*
    1. 修改用户名
    2. 修改密码
    3. 获取用户信息
    4. 上传头像
     */

    @Autowired private UserService userService;

    /**
     * 修改用户名
     * @param userRequest 请求体
     * @return 修改结果
     */
    @PutMapping("/updateName")
    public ResponseEntity<InfoResponse> updateUsername(@RequestBody UserRequest userRequest) {
        log.info("Change username!");
        // 从请求体中获取用户信息
        String origUsername = userRequest.getUsername();
        String newUsername = userRequest.getNewUsername();
        log.info("User name: {}, New user name: {}", origUsername, newUsername);
        // 调用业务逻辑层
        return ResponseEntity.ok(userService.updateUsername(origUsername, newUsername));
    }

    /**
     * 修改密码
     * @param userRequest 请求体
     * @return 修改结果
     */
    @PutMapping("/updatePassword")
    public ResponseEntity<InfoResponse> updatePassword(@RequestBody UserRequest userRequest) {
        log.info("Change password!");
        // 从请求体中获取用户信息
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        String newPassword = userRequest.getNewPassword();
        log.info("Username: {}, Old password: {}, New password: {}", username, password, newPassword);
        // 调用业务逻辑层
        InfoResponse result = userService.updatePassword(username, password, newPassword);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public ResponseEntity<User> getUserInfo(String username) {
        log.info("Get user information!");
        log.info("Username: {}", username);
        // 调用业务逻辑层
        User user = userService.getUserInfo(username);
        return ResponseEntity.ok(user);
    }

    /**
     * 上传头像
     * @return 上传结果
     */
    @PostMapping("/uploadAvatar")
    public ResponseEntity<String> uploadAvatar(MultipartFile file) {
        log.info("Upload your avatar!");
        log.info("File name: {}", file.getOriginalFilename());
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }
}
