package com.nzhk.wxg.common.interceptor;

import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.cache.UserInfo;
import com.nzhk.wxg.common.utils.JwtUtil;
import com.nzhk.wxg.common.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            ResponseUtil.setResponse(response, 500, "登录失效，请重新登录");
        }

        Map<String, Object> jwtMap = null;
        try {
            jwtMap = JwtUtil.parseToken(token);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Token 为空");
        } catch (Exception e) {
            throw new RuntimeException("Token 验证失败：" + e.getMessage());
        }

        Object userIdObj = jwtMap.get("userId");
        if (null == userIdObj) {
            ResponseUtil.setResponse(response, 500, "登录失效，请重新登录");
        }

        UserInfo userInfo = UserInfo.builder().id((String) userIdObj).build();
        ContextCache.setUserInfo(userInfo);
        return true;
    }
}
