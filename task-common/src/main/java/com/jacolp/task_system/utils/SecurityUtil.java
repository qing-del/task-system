package com.jacolp.task_system.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * 获取当前登录用户的用户名
     * 注意：必须在经过 JwtFilter 认证后的请求里调用，否则可能报错或者返回 null
     * @return
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();    // 返回 UserDetails 里的 username
        }
        throw new RuntimeException("当前未登录或者无上下文信息");
    }
}
