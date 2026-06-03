package com.campus.secondhand.common.security.context;

// 当前登录用户上下文，基于 ThreadLocal 实现
// 作用：在同一个请求的任何地方，都可以拿到当前登录用户信息
// 原理：每个请求对应一个线程，ThreadLocal 的数据只有当前线程能访问
// 清理：请求结束后必须在 finally 中调用 clear()，防止线程复用导致数据错乱
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    // 设置当前登录用户（JwtAuthenticationFilter 中调用）
    public static void setUser(Long userId, String username, String role) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
        ROLE_HOLDER.set(role);
    }

    // 获取当前登录用户 id
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    // 获取当前登录用户用户名
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    // 获取当前登录用户角色
    public static String getRole() {
        return ROLE_HOLDER.get();
    }

    // 清理当前用户信息（请求结束后必须调用）
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLE_HOLDER.remove();
    }
}
