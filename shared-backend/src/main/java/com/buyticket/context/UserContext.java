package com.buyticket.context;

/**
 * 用户上下文管理类
 * 使用ThreadLocal存储当前请求的用户信息
 */
public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static void setRole(String role) {
        roleHolder.set(role);
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
