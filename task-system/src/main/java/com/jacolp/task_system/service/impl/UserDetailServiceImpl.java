package com.jacolp.task_system.service.impl; // 假设你的包结构

import com.jacolp.task_system.mapper.UserMapper; // 假设你有 Mapper
import com.jacolp.task_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper; // 这里的 Mapper 需要你自己写查询方法

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户
        // 注意：你需要去 UserMapper 里加一个 findByUsername 方法
        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 将我们的 User 实体转换为 Spring Security 需要的 UserDetails 对象
        // 这里的 org.springframework.security.core.userdetails.User 是 Spring 自带的
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 数据库里的加密密码
                new ArrayList<>() // 权限列表，暂时留空
        );
    }
}