package com.jacolp.task_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;    // 采用 Hash 加密

    private Integer role;   // 0：封号，1：普通用户，2：VIP用户，3：管理员

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt; // 创建时间

    private String headPhoto;   // 头像 -- 阿里云OSS的URL地址
}
