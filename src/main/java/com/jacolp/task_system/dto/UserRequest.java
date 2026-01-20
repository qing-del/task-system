package com.jacolp.task_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String username;
    private String password;

    private String newUsername; // 若是修改用户名就传入这个值
    private String newPassword; // 若是修改密码就传入这个值
}
