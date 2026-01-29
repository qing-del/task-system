package com.jacolp.task_system.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrentUserIdHolder {
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        // 允许设置null，但记录警告
        if (userId == null) {
            log.warn("Attempting to set a null user ID may lead to subsequent logic errors");
        }
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            log.debug("The current thread has not set a user ID");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }

    // 添加一个安全获取方法，带默认值
    public static Long getCurrentUserIdOrDefault(Long defaultValue) {
        Long userId = CURRENT_USER_ID.get();
        return userId != null ? userId : defaultValue;
    }
}
