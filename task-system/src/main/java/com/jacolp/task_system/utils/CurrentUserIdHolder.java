package com.jacolp.task_system.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrentUserIdHolder {
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        // 允许设置null，但记录警告
        if (userId == null) {
            log.warn("尝试设置null用户ID，这可能导致后续逻辑错误");
        }
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            log.debug("当前线程没有设置用户ID");
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
