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
        String requestURI = request.getRequestURI();
        log.info("JwtInterceptor preHandle requestURI:{}", requestURI);

        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            log.warn("requestURI:{} token empty", requestURI);
            ResponseUtil.writeJsonResponse(response, 401, 40100, "登录失效，请重新登录", null);
            return false;
        }
        Map<String, Object> jwtMap;
        try {
            jwtMap = JwtUtil.parseToken(token);
        } catch (IllegalArgumentException e) {
            log.warn("requestURI:{} token parse error, msg: Token 为空", requestURI);
            ResponseUtil.writeJsonResponse(response, 401, 40101, "Token 为空", null);
            return false;
        } catch (Exception e) {
            log.warn("requestURI:{} token verify failed, msg:{}", requestURI, e.getMessage());
            ResponseUtil.writeJsonResponse(response, 401, 40102, "Token 验证失败，请重新登录", null);
            return false;
        }

        Object userIdObj = jwtMap.get("userId");
        if (null == userIdObj) {
            log.warn("requestURI:{} jwt claims userId is null", requestURI);
            ResponseUtil.writeJsonResponse(response, 401, 40100, "登录失效，请重新登录", null);
            return false;
        }

        UserInfo userInfo = UserInfo.builder().id((String) userIdObj).token(token).build();
        log.info("JwtInterceptor userId:{}", userInfo.getId());
        ContextCache.setUserInfo(userInfo);
        return true;
    }
}
