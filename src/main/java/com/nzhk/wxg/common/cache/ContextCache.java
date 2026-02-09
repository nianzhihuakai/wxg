package com.nzhk.wxg.common.cache;


/**
 * 全局当前登录用户信息
 */
public class ContextCache {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    public ContextCache() {
    }

    public static String getUserId() {
        return getUser().getId();
    }

    public static UserInfo getUser() {
        return THREAD_LOCAL.get();
    }

    public static void setUserInfo(UserInfo userInfo) {
        THREAD_LOCAL.set(userInfo);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
