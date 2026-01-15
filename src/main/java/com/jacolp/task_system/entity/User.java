package com.jacolp.task_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;    // 采用 Hash 加密

    private Integer role;   // 0：封号，1：普通用户，2：VIP用户，3：管理员
}
