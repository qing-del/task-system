package com.jacolp.task_system.service.impl;

import com.aliyun.oss.AliyunOSSOperator;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.exception.UserException;
import com.jacolp.task_system.mapper.UserMapper;
import com.jacolp.task_system.service.UserService;
import com.jacolp.task_system.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;    // 密码加密
    @Autowired private AliyunOSSOperator aliyunOSSOperator;

    /**
     * 更新用户名
     * @param origUsername 原用户名
     * @param newUsername 新用户名
     * @return 更新结果
     */
    @Override
    public InfoResponse updateUsername(String origUsername, String newUsername) {
        log.info("Start processing the logic for updating user names...");
        // 1. 获取用户
        User user = getUser(origUsername);

        // 2. 判断新的用户名是否与他人重合
        User otherUser = userMapper.findByUsername(newUsername);
        if (otherUser != null) {
            return new InfoResponse(false, "用户名已存在！");
        }

        // 3. 更新用户名
        user.setUsername(newUsername);
        Integer result = userMapper.update(user);

        // 更新失败
        if (result==null || result==0) {
            log.error("Failed to update the username in the database!");
            throw new UserException("数据库更新用户名失败！");
        }

        return new InfoResponse(true, "更新成功！");
    }

    /**
     * 修改密码
     * @param username 用户名
     * @param password 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    public InfoResponse updatePassword(String username, String password, String newPassword) {
        // 1. 获取用户
        User user = getUser(username);

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Incorrect password!");
            return new InfoResponse(false, "原密码错误！");
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean result = userMapper.update(user) > 0;
        if (!result) {
            log.error("Failed to update password in the database!");
            throw new UserException("数据库更新密码失败！");
        }

        log.info("Update password success!");
        return new InfoResponse(true, "修改成功！");
    }

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User getUserInfo(String username) {
        // 获取用户
        User user = getUser(username);
        user.setPassword(null);
        return user;
    }

    /**
     * 上传头像
     * @param file 文件
     * @return 上传结果 -- 可访问的 url
     */
    @Override
    public String uploadAvatar(MultipartFile file) {
        // 1. 获取文件名
        String originalFilename = file.getOriginalFilename();
        String username = SecurityUtil.getCurrentUsername();
        User user = getUser(username);
        // 2. 上传文件
        try {
            String url = aliyunOSSOperator.upload(file.getBytes(), originalFilename);
            user.setHeadPhoto(url);
            Integer result = userMapper.update(user);

            if (result==null || result==0) {
                log.error("Failed to update the head photo in the database!");
                throw new UserException("数据库更新头像失败！");
            }

            log.info("Update head photo success!");
            return url;
        } catch (IOException e) {
            log.error("Failed to upload file! -- getBytes() appear error!");
            throw new UserException("上传文件失败！");
        } catch (Exception e) {
            log.error("Failed to upload file! -- upload() appear error!");
            throw new UserException("上传文件失败！");
        }
    }

    /**
     * 内部辅助方法
     * 通过用户名获取用户
     * 若是用户不存在会抛出“用户不存在”异常
     * @param username 用户名
     * @return 用户
     */
    private User getUser(String username) {
        User user = userMapper.findByUsername(username);
        // 先判断用户是否存在
        if(user == null) {
            log.error("User does not exist!");
            throw new UserException("用户不存在！");
        }
        return user;
    }
}
