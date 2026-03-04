package com.nzhk.wxg.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wxg.wechat.config.WechatProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信 access_token 获取与缓存
 * 有效期为 7200 秒，提前 5 分钟刷新
 */
@Slf4j
@Service
public class WechatAccessTokenService {

    @Resource
    private WechatProperties wechatProperties;
    @Resource
    private RestTemplate restTemplate;

    private volatile String cachedToken;
    private volatile long expireAt;

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    public String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < expireAt) {
            return cachedToken;
        }
        synchronized (this) {
            if (cachedToken != null && System.currentTimeMillis() < expireAt) {
                return cachedToken;
            }
            refreshToken();
            return cachedToken;
        }
    }

    private void refreshToken() {
        String appid = wechatProperties.getAppid();
        String secret = wechatProperties.getSecret();
        if (appid == null || secret == null || secret.isEmpty()) {
            log.warn("wechat appid or secret not configured, skip refresh token");
            return;
        }
        String url = String.format(TOKEN_URL, appid, secret);
        try {
            String json = restTemplate.getForObject(url, String.class);
            JSONObject obj = JSONObject.parseObject(json);
            if (obj != null && obj.containsKey("access_token")) {
                cachedToken = obj.getString("access_token");
                int expiresIn = obj.getIntValue("expires_in");
                expireAt = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
                log.info("wechat access_token refreshed, expires_in={}", expiresIn);
            } else {
                log.error("wechat token api error: {}", json);
            }
        } catch (Exception e) {
            log.error("wechat token refresh failed", e);
        }
    }
}
