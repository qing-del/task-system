package com.jacolp.task_system.service;

import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UserService {
    /**
     * 修改用户名
     * @param origUsername 原用户名
     * @param newUsername 新用户名
     * @return 修改结果
     */
    InfoResponse updateUsername(String origUsername, String newUsername);

    /**
     * 修改密码
     * @param username 用户名
     * @param password 旧密码
     * @param newPassword 新密码
     * @return
     */
    InfoResponse updatePassword(String username, String password, String newPassword);

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User getUserInfo(String username);

    /**
     * 上传头像
     * @return 上传结果 -- 可访问的 url
     */
    String uploadAvatar(MultipartFile file);
}
